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

import io.dropwizard.setup.Environment;

import java.util.Map;

import javax.servlet.ServletException;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A Guice based Jersey container that can pull resources out of the dropwizard environment.
 */
class DropwizardGuiceContainer extends GuiceContainer
{
    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings("BAD_PRACTICE")
    private final ResourceConfig resourceConfig;

    @Inject
    DropwizardGuiceContainer(final Injector injector, final Environment environment)
    {
        super(injector);

        checkNotNull(environment, "environment is null");
        this.resourceConfig = environment.jersey().getResourceConfig();
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(final Map<String, Object> props, final WebConfig webConfig) throws ServletException
    {
        return resourceConfig;
    }
}
