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

import java.util.Set;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.servlets.tasks.Task;

class DropwizardGuiceModule implements Module
{
    private final ImmutableSet.Builder<Managed> managedBuilder = ImmutableSet.builder();
    private final ImmutableSet.Builder<Task> taskBuilder = ImmutableSet.builder();
    private final ImmutableSet.Builder<HealthCheck> healthcheckBuilder = ImmutableSet.builder();
    private final ImmutableSet.Builder<ServerLifecycleListener> serverLifecycleListenerBuilder = ImmutableSet.builder();

    @Override
    public void configure(final Binder binder)
    {
        binder.bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <T> void hear(TypeLiteral<T> type, TypeEncounter<T> encounter)
            {
                encounter.register(new InjectionListener<T>() {
                    @Override
                    public void afterInjection(T obj)
                    {
                        // Don't 'optimize' to else if here; an object may implement
                        // more than one interface (e.g. {@link Managed} and {@link HealthCheck}.

                        if (obj instanceof Managed) {
                            managedBuilder.add((Managed) obj);
                        }

                        if (obj instanceof Task) {
                            taskBuilder.add((Task) obj);
                        }

                        if (obj instanceof HealthCheck) {
                            healthcheckBuilder.add((HealthCheck) obj);
                        }

                        if (obj instanceof ServerLifecycleListener) {
                            serverLifecycleListenerBuilder.add((ServerLifecycleListener) obj);
                        }
                    }
                });
            }
        });
    }

    Set<Managed> getManaged()
    {
        return managedBuilder.build();
    }

    Set<Task> getTasks()
    {
        return taskBuilder.build();
    }

    Set<HealthCheck> getHealthChecks()
    {
        return healthcheckBuilder.build();
    }

    Set<ServerLifecycleListener> getServerLifecycleListeners()
    {
        return serverLifecycleListenerBuilder.build();
    }
}
