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
import griffon.plugins.preferences.PreferencesNode;

import javax.application.converter.ConverterRegistry;

/**
 * @author Andres Almiray
 */
public class DefaultPreferences extends AbstractPreferences {
    private final PreferencesNode root;

    public DefaultPreferences(@Nonnull ConverterRegistry converterRegistry) {
        super(converterRegistry);
        root = new DefaultPreferencesNode(this, null, PreferencesNode.PATH_SEPARATOR);
    }

    @Nonnull
    public PreferencesNode getRoot() {
        return root;
    }

    @Nonnull
    public Preferences copy() {
        Preferences copy = new DefaultPreferences(getConverterRegistry());
        copy.getRoot().merge(root);
        return copy;
    }
}
