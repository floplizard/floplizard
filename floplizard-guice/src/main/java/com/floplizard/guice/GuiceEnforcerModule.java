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

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Enforces good Guice behavior.
 *
 *  <ul>
 *  <li>disables circular injection</li>
 *  <li>requires all bindings to be explicitly configured</li>
 *  <li>requires binding annotations to match exactly</li>
 *  <li>requires {@link Inject} annotations for all constructors</li>
 *  </ul>
 */
public class GuiceEnforcerModule implements Module
{
    @Override
    public void configure(final Binder binder)
    {
        binder.disableCircularProxies();
        binder.requireExplicitBindings();
        binder.requireExactBindingAnnotations();
        binder.requireAtInjectOnConstructors();
    }
}
