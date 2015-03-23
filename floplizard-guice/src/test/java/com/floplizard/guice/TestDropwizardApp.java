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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.ClassRule;
import org.junit.Test;

import io.dropwizard.testing.junit.DropwizardAppRule;

public class TestDropwizardApp
{
    @ClassRule
    public static final DropwizardAppRule<LittleGuiceAppConfiguration> RULE = new DropwizardAppRule<LittleGuiceAppConfiguration>(LittleGuiceApp.class, ResourceHelpers.resourceFilePath("little-guice-app-config.yaml"));

    @Test
    public void testAppInjection()
    {
        assertEquals("this-is-a-constant", ((LittleGuiceApp) RULE.getApplication()).getConstantName());
    }

    @Test
    public void testDropwizardAwareModule()
    {
        // injected in LittleAppModule by pulling it out of configuration and binding it to a constant.
        assertEquals("application-name", ((LittleGuiceApp) RULE.getApplication()).getAppName());
    }

    @Test
    public void testBoundDropwizardStuff()
    {
        LittleGuiceApp app = (LittleGuiceApp) RULE.getApplication();

        assertNotNull(app.getInjectedConfiguration());
        assertNotNull(app.getInjectedEnvironment());

        assertSame(app.getInjectedConfiguration(), RULE.getConfiguration());
        assertSame(app.getInjectedEnvironment(), RULE.getEnvironment());
    }

    @Test
    public void testObjectMapperProvider()
    {
        LittleGuiceApp app = (LittleGuiceApp) RULE.getApplication();

        assertNotNull(app.getObjectMapper());

        assertNotSame(app.getObjectMapper(), RULE.getEnvironment().getObjectMapper());
    }
}
