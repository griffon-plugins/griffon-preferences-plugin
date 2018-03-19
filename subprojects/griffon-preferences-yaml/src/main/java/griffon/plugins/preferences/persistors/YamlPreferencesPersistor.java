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
package griffon.plugins.preferences.persistors;

import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;
import griffon.plugins.preferences.Preferences;
import griffon.plugins.preferences.PreferencesManager;
import griffon.plugins.preferences.PreferencesNode;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public class YamlPreferencesPersistor extends AbstractMapBasedPreferencesPersistor {
    @Inject
    public YamlPreferencesPersistor(@Nonnull GriffonApplication application, @Nonnull Metadata metadata) {
        super(application, metadata);
    }

    @Nonnull
    @Override
    protected String resolveExtension() {
        return ".yaml";
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public Preferences read(@Nonnull PreferencesManager preferencesManager) throws IOException {
        InputStream inputStream = inputStream();
        Map<String, Object> yaml = doRead(inputStream);
        inputStream.close();
        PreferencesNode node = preferencesManager.getPreferences().getRoot();
        readInto(yaml, node);

        return preferencesManager.getPreferences();
    }

    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    protected void readInto(@Nonnull Map<String, Object> yaml, @Nonnull PreferencesNode node) {
        for (Map.Entry<String, Object> e : yaml.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            if (value instanceof Map) {
                readInto((Map<String, Object>) value, node.node(key));
            } else if (value instanceof List) {
                try {
                    node.putAt(key, expand((List<Object>) value));
                } catch (IllegalArgumentException iae) {
                    throw new IllegalArgumentException("Invalid value for '" + node.path() + "." + key + "' => " + value, iae);
                }
            } else if (value instanceof Number ||
                value instanceof Boolean ||
                value instanceof CharSequence) {
                node.putAt(key, value);
            } else {
                throw new IllegalArgumentException("Invalid value for '" + node.path() + "." + key + "' => " + value);
            }
        }
    }

    @Nonnull
    protected Collection expand(@Nonnull List<Object> array) {
        List<Object> list = new ArrayList<>();
        for (Object element : array) {
            if (element instanceof Number ||
                element instanceof Boolean ||
                element instanceof CharSequence) {
                list.add(element);
            } else {
                throw new IllegalArgumentException("Invalid value: " + element);
            }
        }

        return list;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    protected Map<String, Object> doRead(@Nonnull InputStream inputStream) throws IOException {
        if (inputStream.available() > 0) {
            return setupYamlForRead().loadAs(inputStream, Map.class);
        }
        return Collections.emptyMap();
    }

    @Override
    protected void write(@Nonnull Map<String, Object> map, @Nonnull OutputStream outputStream) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        writer.write(setupYamlForWrite().dumpAsMap(map));
        writer.flush();
        writer.close();
    }

    @Nonnull
    protected Yaml setupYamlForRead() {
        return new Yaml();
    }

    @Nonnull
    protected Yaml setupYamlForWrite() {
        return new Yaml();
    }
}
