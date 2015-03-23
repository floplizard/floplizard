/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.floplizard.guice;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nonnull;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Dropwizard bundle that adds basic guice integration.
 */
public class GuiceBundle<T extends Configuration> implements ConfiguredBundle<T>
{
    private static final Logger LOG = LoggerFactory.getLogger(GuiceBundle.class);

    /**
     * Returns a builder for the Guice bundle.
     */
    public static final <U extends Configuration> Builder<U> defaultBuilder(final Class<U> configClass)
    {
        return new Builder<>(configClass);
    }

    private final Class<T> configClass;
    private final ImmutableSet<Module> guiceModules;
    private final Stage guiceStage;
    private final boolean enforcerEnabled;

    private GuiceBundle(final Class<T> configClass, final ImmutableSet<Module> guiceModules, final Stage guiceStage, final boolean enforcerEnabled)
    {
        this.configClass = configClass;

        this.guiceModules = guiceModules;
        this.guiceStage = guiceStage;
        this.enforcerEnabled = enforcerEnabled;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap)
    {
    }

    @Override
    public void run(final T configuration, final Environment environment) throws Exception
    {
        for (Module module : guiceModules) {
            if (module instanceof DropwizardAwareModule<?>) {
                @SuppressWarnings("unchecked")
                DropwizardAwareModule<T> dropwizardAwareModule = (DropwizardAwareModule<T>) module;
                dropwizardAwareModule.setDropwizardConfiguration(configuration, environment);
            }
        }

        final DropwizardGuiceModule dropwizardGuiceModule = new DropwizardGuiceModule();

        ImmutableSet.Builder<Module> moduleBuilder = ImmutableSet.builder();
        moduleBuilder.addAll(guiceModules);
        moduleBuilder.add(new JerseyServletModule());
        moduleBuilder.add(dropwizardGuiceModule);
        moduleBuilder.add(new Module() {
            @Override
            public void configure(final Binder binder)
            {
                binder.bind(Environment.class).toInstance(environment);
                binder.bind(configClass).toInstance(configuration);

                binder.bind(GuiceContainer.class).to(DropwizardGuiceContainer.class).in(Scopes.SINGLETON);
            }
        });

        if (enforcerEnabled) {
            moduleBuilder.add(new GuiceEnforcerModule());
        }

        final Injector injector = Guice.createInjector(guiceStage, moduleBuilder.build());

        for (Managed managed : dropwizardGuiceModule.getManaged()) {
            LOG.info("Added guice injected managed Object: {}", managed.getClass().getName());
            environment.lifecycle().manage(managed);
        }

        for (Task task : dropwizardGuiceModule.getTasks()) {
            environment.admin().addTask(task);
            LOG.info("Added guice injected Task: {}", task.getClass().getName());
        }

        for (HealthCheck healthcheck : dropwizardGuiceModule.getHealthChecks()) {
            environment.healthChecks().register(healthcheck.getClass().getSimpleName(), healthcheck);
            LOG.info("Added guice injected health check: {}", healthcheck.getClass().getName());
        }

        for (ServerLifecycleListener serverLifecycleListener : dropwizardGuiceModule.getServerLifecycleListeners()) {
            environment.lifecycle().addServerLifecycleListener(serverLifecycleListener);
            LOG.info("Added guice server lifecycle listener: {}", serverLifecycleListener.getClass().getName());
        }

        addJerseyBindings(environment, injector, ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, ContainerRequestFilter.class);
        addJerseyBindings(environment, injector, ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, ContainerResponseFilter.class);
        addJerseyBindings(environment, injector, ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES, ResourceFilterFactory.class);

        environment.jersey().replace(getReplacerFunction(injector.getInstance(GuiceContainer.class)));
        environment.servlets().addFilter("Guice Filter", GuiceFilter.class).addMappingForUrlPatterns(null, false, environment.getApplicationContext().getContextPath() + "*");
    }

    private static <T> void addJerseyBindings(Environment environment, Injector injector, String propertyName, Class<T> clazz)
    {
        final TypeToken<Set<T>> setToken = new TypeToken<Set<T>>() {}.where(new TypeParameter<T>() {}, clazz);

        @SuppressWarnings("unchecked")
        final Key<Set<T>> key = (Key<Set<T>>) Key.get(setToken.getType());

        final Binding<? super Set<T>> binding = injector.getExistingBinding(key);

        if (binding != null) {
            environment.jersey().property(propertyName, ImmutableList.copyOf(injector.getInstance(key)));
        }
    }

    private static Function<ResourceConfig, ServletContainer> getReplacerFunction(final GuiceContainer container)
    {
        checkNotNull(container, "container is null");
        return new Function<ResourceConfig, ServletContainer>() {
            @Override
            public ServletContainer apply(@Nonnull final ResourceConfig resourceConfig)
            {
                return container;
            }
        };
    }

    public static final class Builder<U extends Configuration>
    {
        private final Class<U> configClass;
        private final ImmutableSet.Builder<Module> guiceModules = ImmutableSet.builder();
        private Stage guiceStage = Stage.PRODUCTION;
        private boolean enforcerEnabled = true;

        private Builder(final Class<U> configClass)
        {
            this.configClass = configClass;
        }

        /**
         * Sets the Guice {@link Stage} for injection. If unset, use {@link Stage.PRODUCTION}.
         */
        public Builder<U> stage(final Stage guiceStage)
        {
            checkNotNull(guiceStage, "guiceStage is null");
            if (guiceStage != Stage.PRODUCTION) {
                LOG.warn("*** Guice stage was set to {}. Guice should only ever run in PRODUCTION mode except for testing!", guiceStage);
            }
            this.guiceStage = guiceStage;
            return this;
        }

        public Builder<U> disableEnforcer()
        {
            enforcerEnabled = false;
            LOG.warn("*** The Guice enforcement module was disabled. This can lead to problems with dependency injection and application stability!");

            return this;
        }

        /**
         * Adds guice modules to the bundle.
         */
        public Builder<U> modules(final Module ... modules)
        {
            return modules(Arrays.asList(modules));
        }

        /**
         * Adds guice modules to the bundle.
         */
        public Builder<U> modules(final Iterable<? extends Module> modules)
        {
            guiceModules.addAll(modules);

            return this;
        }

        /**
         * Returns a Bundle that can be added to the bootstrap.
         */
        public GuiceBundle<U> build()
        {
            return new GuiceBundle<U>(configClass, guiceModules.build(), guiceStage, enforcerEnabled);
        }
    }
}
