/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2021 The author and/or original authors.
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
package griffon.plugins.preferences;

import griffon.annotations.core.Nonnull;

import javax.application.converter.ConverterRegistry;

/**
 * @author Andres Almiray
 */
public interface Preferences extends NodeChangeListener, PreferenceChangeListener {
    @Nonnull
    ConverterRegistry getConverterRegistry();

    void addNodeChangeListener(@Nonnull NodeChangeListener listener);

    void removeNodeChangeListener(@Nonnull NodeChangeListener listener);

    @Nonnull
    NodeChangeListener[] getNodeChangeListeners();

    void addPreferencesChangeListener(@Nonnull PreferenceChangeListener listener);

    void removePreferencesChangeListener(@Nonnull PreferenceChangeListener listener);

    @Nonnull
    PreferenceChangeListener[] getPreferencesChangeListeners();

    @Nonnull
    PreferencesNode getRoot();

    boolean containsNode(@Nonnull Class<?> clazz);

    boolean containsNode(@Nonnull String path);

    PreferencesNode node(@Nonnull Class<?> clazz);

    PreferencesNode node(@Nonnull String path);

    PreferencesNode removeNode(@Nonnull Class<?> clazz);

    PreferencesNode removeNode(@Nonnull String path);

    @Nonnull
    Preferences copy();
}
