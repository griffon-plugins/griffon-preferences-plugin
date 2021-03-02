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
package org.codehaus.griffon.runtime.preferences

import com.acme.SampleModel
import griffon.annotations.core.Nonnull
import griffon.core.GriffonApplication
import griffon.core.env.Metadata
import griffon.core.injection.Module
import griffon.plugins.preferences.PreferencesManager
import griffon.plugins.preferences.PreferencesPersistor
import griffon.plugins.preferences.persistors.AbstractMapBasedPreferencesPersistor
import griffon.test.core.GriffonUnitRule
import org.codehaus.griffon.runtime.core.injection.AbstractModule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import javax.application.converter.ConverterRegistry
import javax.inject.Inject
import java.text.SimpleDateFormat

import static griffon.util.ConfigUtils.getConfigValueAsBoolean
import static griffon.util.ConfigUtils.getConfigValueAsString

class TypeFormattingTest {
    static {
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'trace')
        System.setProperty('griffon.full.stacktrace', 'true')
    }

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject
    private PreferencesManager preferencesManager

    @Inject
    private GriffonApplication application

    @Inject
    private PreferencesPersistor preferencesPersistor

    private Date january2K

    @Before
    void setup() {
        january2K = new SimpleDateFormat('dd/MM/yyyy').parse('01/01/2000')

        ((InMemoryPreferencesPersistor) preferencesPersistor).map.putAll(
            [
                com: [
                    acme: [
                        SampleModel: [
                            pstring: 'string'

                        ]
                    ]
                ]
            ]
        )
        preferencesPersistor.read(preferencesManager)
    }

    @Test
    void preferencesAreInjectedAsExpected() {
        // given:
        SampleModel model = application.artifactManager.newInstance(SampleModel)

        // expect:
        assert model.pstring == 'string'
        assert model.pboolean
        assert model.pdate == january2K
        assert model.customString == 'value'

        // when:
        preferencesManager.preferences.node(SampleModel)['pstring'] = 'STRING'

        // expect:
        assert model.pstring == 'STRING'
    }

    @Test
    void writeFromModelToPreferences() {
        // given:
        SampleModel model = application.artifactManager.newInstance(SampleModel)

        // expect:
        assert model.pstring == 'string'

        // when:
        model.pstring = 'STRING'
        model.pboolean = false
        model.pdate = new SimpleDateFormat('dd/MM/yyyy').parse('12/12/2012')
        model.customString = 'griffon'
        preferencesManager.save(model)
        preferencesPersistor.write(preferencesManager)

        // expect:
        Map map = ((InMemoryPreferencesPersistor) preferencesPersistor).map
        assert 'STRING' == getConfigValueAsString(map, 'com.acme.SampleModel.pstring')
        assert !getConfigValueAsBoolean(map, 'com.acme.SampleModel.pboolean')
        assert '12/12/2012' == getConfigValueAsString(map, 'com.acme.SampleModel.pdate')
        assert '*griffon*' == getConfigValueAsString(map, 'com.acme.SampleModel.customString')
    }

    private static class InMemoryPreferencesPersistor extends AbstractMapBasedPreferencesPersistor {
        final Map<String, Object> map = [:]

        @Inject
        InMemoryPreferencesPersistor(@Nonnull GriffonApplication application,
                                     @Nonnull Metadata metadata,
                                     @Nonnull ConverterRegistry converterRegistry) {
            super(application, metadata, converterRegistry)
        }

        @Nonnull
        @Override
        protected InputStream inputStream() throws IOException {
            return System.in
        }

        @Nonnull
        @Override
        protected OutputStream outputStream() throws IOException {
            return System.out
        }

        @Nonnull
        @Override
        protected Map<String, Object> read(@Nonnull InputStream inputStream) throws IOException {
            return map
        }

        @Override
        protected void write(@Nonnull Map<String, Object> map, @Nonnull OutputStream outputStream) throws IOException {
            this.map.clear()
            this.map.putAll(map)
        }
    }

    @Nonnull
    private List<Module> moduleOverrides() {
        [
            new AbstractModule() {
                @Override
                protected void doConfigure() {
                    bind(PreferencesPersistor)
                        .to(InMemoryPreferencesPersistor)
                        .asSingleton()
                }
            }
        ]
    }
}
