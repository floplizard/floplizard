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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;

import io.dropwizard.setup.Environment;

/**
 * Expose the Dropwizard Object mapper into Guice. To use this, add a
 * binding into a guice module: <br>
 *
 * <pre>
 *     binder.bind(ObjectMapper.class).toProvider(DropwizardObjectMapperProvider.class);
 * </pre>
 *
 * If all parts of the application should receive the same {@link ObjectMapper} instance,
 * bind in Singleton scope. The ObjectMapper injected is a copy of the dropwizard internal
 * Object mapper, not the same instance as {@link Environment.getObjectMapper()} returns.
 */
public class DropwizardObjectMapperProvider implements Provider<ObjectMapper>
{
    private final Environment environment;

    @Inject
    DropwizardObjectMapperProvider(final Environment environment)
    {
        this.environment = checkNotNull(environment, "environment is null");
    }

    @Override
    public ObjectMapper get()
    {
        return environment.getObjectMapper().copy();
    }
}
