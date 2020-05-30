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
package griffon.plugins.preferences.persistors;

import griffon.annotations.core.Nonnull;
import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;

import javax.application.converter.ConverterRegistry;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public class SerializingPreferencesPersistor extends AbstractMapBasedPreferencesPersistor {
    @Inject
    public SerializingPreferencesPersistor(@Nonnull GriffonApplication application,
                                           @Nonnull Metadata metadata,
                                           @Nonnull ConverterRegistry converterRegistry) {
        super(application, metadata, converterRegistry);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Object> read(@Nonnull InputStream inputStream) throws IOException {
        try {
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            Object o = ois.readObject();
            return (Map<String, Object>) o;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    protected void write(@Nonnull Map<String, Object> map, @Nonnull OutputStream outputStream) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(map);
    }
}
