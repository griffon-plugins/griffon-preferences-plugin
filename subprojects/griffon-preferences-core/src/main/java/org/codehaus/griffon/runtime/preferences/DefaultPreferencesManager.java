/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2020 The author and/or original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.preferences;

import griffon.annotations.core.Nonnull;
import griffon.plugins.preferences.Preferences;

import javax.application.converter.ConverterRegistry;
import javax.inject.Inject;

/**
 * @author Andres Almiray
 */
public class DefaultPreferencesManager extends AbstractPreferencesManager {
    private final Preferences preferences;

    @Inject
    public DefaultPreferencesManager(@Nonnull ConverterRegistry converterRegistry) {
        super(converterRegistry);
        preferences = new DefaultPreferences(converterRegistry);
    }

    @Nonnull
    public Preferences getPreferences() {
        return preferences;
    }
}
