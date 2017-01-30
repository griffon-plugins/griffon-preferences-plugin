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
import griffon.plugins.preferences.Preferences;
import griffon.plugins.preferences.PreferencesManager;
import griffon.plugins.preferences.PreferencesNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public class JsonPreferencesPersistor extends AbstractMapBasedPreferencesPersistor {
    @Inject
    public JsonPreferencesPersistor(@Nonnull GriffonApplication application) {
        super(application);
    }

    @Nonnull
    @Override
    protected String resolveExtension() {
        return ".json";
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public Preferences read(@Nonnull PreferencesManager preferencesManager) throws IOException {
        JSONObject json = doRead(inputStream());
        PreferencesNode node = preferencesManager.getPreferences().getRoot();
        readInto(json, node);

        return preferencesManager.getPreferences();
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    protected void readInto(@Nonnull JSONObject json, @Nonnull PreferencesNode node) {
        for (Object k : json.keySet()) {
            String key = String.valueOf(k);
            Object value = json.get(key);
            if (value instanceof JSONObject) {
                readInto((JSONObject) value, node.node(key));
            } else if (value instanceof JSONArray) {
                try {
                    node.putAt(key, expand((JSONArray) value));
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
    protected Collection expand(@Nonnull JSONArray array) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object element = array.get(i);
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
    protected JSONObject doRead(@Nonnull InputStream inputStream) throws IOException {
        if (inputStream.available() > 0) {
            return new JSONObject(new JSONTokener(inputStream));
        }
        return new JSONObject();
    }

    @Override
    protected void write(@Nonnull Map<String, Object> map, @Nonnull OutputStream outputStream) throws IOException {
        JSONObject json = new JSONObject(map);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        writer.write(json.toString(4));
        writer.flush();
    }
}
