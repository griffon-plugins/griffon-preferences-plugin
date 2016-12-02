/*
 * Copyright 2014-2016 the original author or authors.
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
import griffon.core.editors.PropertyEditorResolver;
import griffon.core.env.Metadata;
import griffon.plugins.preferences.Preferences;
import griffon.plugins.preferences.PreferencesManager;
import griffon.plugins.preferences.PreferencesNode;
import griffon.plugins.preferences.PreferencesPersistor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public abstract class AbstractMapBasedPreferencesPersistor implements PreferencesPersistor {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMapBasedPreferencesPersistor.class);
    public static final String KEY_PREFERENCES_PERSISTOR_LOCATION = "preferences.persistor.location";
    public static final String DEFAULT_EXTENSION = ".prefs";

    protected final GriffonApplication application;

    @Inject
    protected Metadata metadata;

    @Inject
    public AbstractMapBasedPreferencesPersistor(@Nonnull GriffonApplication application) {
        this.application = requireNonNull(application, "Argument 'application' cannot ne null");
    }

    @Nonnull
    protected InputStream inputStream() throws IOException {
        File file = resolveFile(resolvePreferencesFileName());
        if (LOG.isInfoEnabled()) {
            LOG.info("Reading preferences from " + file.getAbsolutePath());
        }
        return new FileInputStream(file);
    }

    @Nonnull
    protected OutputStream outputStream() throws IOException {
        File file = resolveFile(resolvePreferencesFileName());
        if (LOG.isInfoEnabled()) {
            LOG.info("Writing preferences to " + file.getAbsolutePath());
        }
        return new FileOutputStream(file);
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    protected String resolvePreferencesFileName() {
        String defaultLocation = System.getProperty("user.home") +
            File.separator + "." +
            metadata.getApplicationName() +
            File.separator +
            "preferences" +
            File.separator +
            "default" +
            resolveExtension();
        return application.getConfiguration().getAsString(
            KEY_PREFERENCES_PERSISTOR_LOCATION,
            defaultLocation);
    }

    @Nonnull
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected File resolveFile(@Nonnull String fileName) {
        File file = new File(fileName);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.home") +
                File.separator + "." +
                metadata.getApplicationName() +
                File.separator +
                "preferences" +
                File.separator +
                fileName);
        }
        if (!file.exists()) file.getParentFile().mkdirs();
        return file;
    }

    @Nonnull
    protected String resolveExtension() {
        return DEFAULT_EXTENSION;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public Preferences read(@Nonnull PreferencesManager preferencesManager) throws IOException {
        InputStream inputStream = inputStream();
        Map<String, Object> map = read(inputStream);
        inputStream.close();
        PreferencesNode node = preferencesManager.getPreferences().getRoot();
        readInto(map, node);

        return preferencesManager.getPreferences();
    }

    @Nonnull
    protected Map<String, Object> read(@Nonnull InputStream inputStream) throws IOException {
        return Collections.emptyMap();
    }

    public void write(@Nonnull PreferencesManager preferencesManager) throws IOException {
        PreferencesNode node = preferencesManager.getPreferences().getRoot();
        Map<String, Object> map = new LinkedHashMap<>();
        writeTo(node, map);
        OutputStream outputStream = outputStream();
        write(map, outputStream);
        outputStream.flush();
        outputStream.close();
    }

    protected abstract void write(@Nonnull Map<String, Object> map, @Nonnull OutputStream outputStream) throws IOException;

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    protected void readInto(@Nonnull Map<String, Object> map, @Nonnull PreferencesNode node) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                readInto((Map<String, Object>) value, node.node(key));
            } else if (value instanceof List ||
                value instanceof Number ||
                value instanceof Boolean ||
                value instanceof CharSequence) {
                node.putAt(key, value);
            } else {
                throw new IllegalArgumentException("Invalid value for '" + node.path() + "." + key + "' => " + value);
            }
        }
    }

    protected void writeTo(PreferencesNode node, Map<String, Object> map) {
        for (String key : node.keys()) {
            Object value = node.getAt(key);
            if (value != null) {
                map.put(key, convertValue(value));
            }
        }
        for (Map.Entry<String, PreferencesNode> child : node.children().entrySet()) {
            Map<String, Object> childMap = new LinkedHashMap<>();
            writeTo(child.getValue(), childMap);
            map.put(child.getKey(), childMap);
        }
    }

    protected Object convertValue(Object value) {
        if (value == null ||
            value instanceof Boolean ||
            value instanceof Number ||
            value instanceof CharSequence) {
            return value;
        }

        if (value instanceof Map) {
            Map<String, Object> tmp = new LinkedHashMap<>();
            Map source = (Map) value;
            for (Object key : source.keySet()) {
                Object val = source.get(key);
                if (val != null) {
                    tmp.put(String.valueOf(key), convertValue(val));
                }
            }
            return tmp;
        } else if (value instanceof Collection) {
            List<Object> tmp = new ArrayList<>();
            List source = (List) value;
            for (Object val : source) {
                tmp.add(convertValue(val));
            }
            return tmp;
        } else if (value.getClass().isArray()) {
            List<Object> tmp = new ArrayList<>();
            Object[] source = (Object[]) value; // blindly cast to Object[]
            for (Object val : source) {
                tmp.add(convertValue(val));
            }
            return tmp;
        } else {
            PropertyEditor propertyEditor = PropertyEditorResolver.findEditor(value.getClass());
            propertyEditor.setValue(value);
            return propertyEditor.getAsText();
        }
    }
}
