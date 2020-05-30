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
package griffon.plugins.preferences.persistors.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import griffon.annotations.core.Nonnull;
import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;
import griffon.plugins.preferences.Preferences;
import griffon.plugins.preferences.PreferencesManager;
import griffon.plugins.preferences.PreferencesNode;
import griffon.plugins.preferences.persistors.AbstractMapBasedPreferencesPersistor;

import javax.application.converter.ConverterRegistry;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import static griffon.util.GriffonNameUtils.isBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class JacksonPreferencesPersistor extends AbstractMapBasedPreferencesPersistor {
    private final ObjectMapper objectMapper;
    private String extension;

    @Inject
    public JacksonPreferencesPersistor(@Nonnull GriffonApplication application,
                                       @Nonnull Metadata metadata,
                                       @Nonnull ConverterRegistry converterRegistry,
                                       @Nonnull ObjectMapper objectMapper) {
        super(application, metadata, converterRegistry);
        this.objectMapper = requireNonNull(objectMapper, "Argument 'objectMapper' must not be null");
        String formatName = objectMapper.getFactory().getFormatName();
        extension = "." + (isBlank(formatName) ? "bin" : formatName.toLowerCase());
    }

    @Nonnull
    @Override
    protected String resolveExtension() {
        return extension;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public Preferences read(@Nonnull PreferencesManager preferencesManager) throws IOException {
        InputStream inputStream = inputStream();
        Map<String, Object> map = doRead(inputStream);
        inputStream.close();
        PreferencesNode node = preferencesManager.getPreferences().getRoot();
        readInto(map, node);

        return preferencesManager.getPreferences();
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    protected Map<String, Object> doRead(@Nonnull InputStream inputStream) throws IOException {
        if (inputStream.available() > 0) {
            return objectMapper.readValue(inputStream, Map.class);
        }
        return Collections.emptyMap();
    }

    @Override
    protected void write(@Nonnull Map<String, Object> map, @Nonnull OutputStream outputStream) throws IOException {
        outputStream.write(objectMapper.writeValueAsBytes(map));
    }
}
