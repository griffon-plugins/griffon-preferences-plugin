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
import griffon.plugins.preferences.PreferencesPersistor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.application.converter.ConverterRegistry;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public abstract class AbstractPreferencesPersistor implements PreferencesPersistor {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPreferencesPersistor.class);

    public static final String KEY_PREFERENCES_PERSISTOR_LOCATION = "preferences.persistor.location";
    public static final String DEFAULT_EXTENSION = ".prefs";

    protected final GriffonApplication application;
    protected final Metadata metadata;
    protected ConverterRegistry converterRegistry;

    @Inject
    public AbstractPreferencesPersistor(@Nonnull GriffonApplication application,
                                        @Nonnull Metadata metadata,
                                        @Nonnull ConverterRegistry converterRegistry) {
        this.application = requireNonNull(application, "Argument 'application' cannot ne null");
        this.metadata = requireNonNull(metadata, "Argument 'metadata' cannot ne null");
        this.converterRegistry = requireNonNull(converterRegistry, "Argument 'converterRegistry' cannot ne null");
    }

    @Nonnull
    protected InputStream inputStream() throws IOException {
        File file = resolveFile(resolvePreferencesFileName());
        LOG.trace("Reading preferences from {}", file.getAbsolutePath());
        return new FileInputStream(file);
    }

    @Nonnull
    protected OutputStream outputStream() throws IOException {
        File file = resolveFile(resolvePreferencesFileName());
        LOG.trace("Writing preferences to {}", file.getAbsolutePath());
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
        if (!file.exists()) { file.getParentFile().mkdirs(); }
        return file;
    }

    @Nonnull
    protected String resolveExtension() {
        return DEFAULT_EXTENSION;
    }
}
