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

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class LittleGuiceApp extends Application<LittleGuiceAppConfiguration>
{
    @Inject
    @Named("a-constant")
    private volatile String constantName = "unset";

    @Inject
    @Named("app-name")
    private volatile String appName = "unset";

    @Inject
    private volatile LittleGuiceAppConfiguration configuration = null;

    @Inject
    private volatile Environment environment = null;

    @Inject
    @Named("app-object-mapper")
    private volatile ObjectMapper objectMapper = null;

    @Override
    public void initialize(final Bootstrap<LittleGuiceAppConfiguration> bootstrap)
    {
        final GuiceBundle<LittleGuiceAppConfiguration> guiceBundle = GuiceBundle.defaultBuilder(LittleGuiceAppConfiguration.class)
            .modules(new Module() {
                @Override
                public void configure(final Binder binder)
                {
                    binder.requestInjection(LittleGuiceApp.this);
                    // In main class bound constant.
                    binder.bindConstant().annotatedWith(Names.named("a-constant")).to("this-is-a-constant");
                    // required, otherwise dw explodes with "no root resource bound".
                    binder.bind(DummyResource.class);

                    binder.bind(ObjectMapper.class).annotatedWith(Names.named("app-object-mapper")).toProvider(DropwizardObjectMapperProvider.class);
                }
            })
            .modules(new LittleGuiceAppModule())
            .build();

        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(final LittleGuiceAppConfiguration configuration, final Environment environment) throws Exception
    {
    }

    public String getConstantName()
    {
        return constantName;
    }

    public String getAppName()
    {
        return appName;
    }

    public LittleGuiceAppConfiguration getInjectedConfiguration()
    {
        return configuration;
    }

    public Environment getInjectedEnvironment()
    {
        return environment;
    }

    public ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

    @Path("/dummy")
    public static class DummyResource
    {
        @Inject
        public DummyResource()
        {
        }

        @GET
        public Response getDummy() {
            return Response.ok("dummy").build();
        }
    }

    public static void main(final String[] args) throws Exception
    {
        new LittleGuiceApp().run(args);
    }
}
