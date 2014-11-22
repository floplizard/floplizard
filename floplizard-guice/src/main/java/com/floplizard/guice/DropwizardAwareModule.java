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
import static com.google.common.base.Preconditions.checkState;

import io.dropwizard.setup.Environment;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * A Guice module that need access to the dropwizard configuration object or the {@link Environment} at
 * binding time can extend this class.
 */
public abstract class DropwizardAwareModule<Configuration> implements Module
{
    private volatile Configuration configuration = null;
    private volatile Environment environment = null;

    @Override
    public final void configure(Binder binder)
    {
        configure(binder, getConfiguration(), getEnvironment());
    }

    final void setDropwizardConfiguration(Configuration configuration, Environment environment)
    {
        checkState(this.configuration == null, "configuration was already set!");
        checkState(this.environment == null, "environment was already set!");
        this.configuration = checkNotNull(configuration, "configuration is null");
        this.environment = checkNotNull(environment, "environment is null");
    }

    /**
     * Access to the configuration for methods in subclasses that are outside {@link Module#configure(Binder)}.
     */
    protected final Configuration getConfiguration()
    {
        return checkNotNull(this.configuration, "configuration was not set!");
    }

    /**
     * Access to the environment for methods in subclasses that are outside {@link Module#configure(Binder)}.
     */
    protected final Environment getEnvironment()
    {
        return checkNotNull(this.environment, "environment was not set!");
    }

    /**
     * Allows access to configuration and environment at binding time.

     * @param binder The Guice {@link Binder}.
     * @param configuration The dropwizard configuration.
     * @param environment The dropwizard environment.
     */
    protected abstract void configure(final Binder binder, final Configuration configuration, final Environment environment);
}
