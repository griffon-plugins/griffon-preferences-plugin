/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.preferences;

import griffon.core.injection.Module;
import griffon.inject.DependsOn;
import griffon.plugins.preferences.PreferencesPersistor;
import griffon.plugins.preferences.persistors.JsonPreferencesPersistor;
import org.codehaus.griffon.runtime.core.injection.AbstractModule;
import org.kordamp.jipsy.ServiceProviderFor;

import javax.inject.Named;

/**
 * @author Andres Almiray
 */
@DependsOn("preferences")
@Named("preferences-json")
@ServiceProviderFor(Module.class)
public class PreferencesJsonModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        // tag::bindings[]
        bind(PreferencesPersistor.class)
            .to(JsonPreferencesPersistor.class)
            .asSingleton();
        // end::bindings[]
    }
}
