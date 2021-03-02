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
package griffon.plugins.preferences.persistors.json

import com.acme.SampleModel
import griffon.annotations.core.Nonnull
import griffon.annotations.inject.DependsOn
import griffon.core.GriffonApplication
import griffon.core.env.Metadata
import griffon.core.injection.Module
import griffon.plugins.preferences.PreferencesManager
import griffon.plugins.preferences.PreferencesPersistor
import griffon.test.core.GriffonUnitRule
import groovy.json.JsonSlurperClassic
import org.codehaus.griffon.test.core.injection.AbstractTestingModule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import javax.application.converter.ConverterRegistry
import javax.inject.Inject
import java.text.SimpleDateFormat

@DependsOn('preferences-json')
class JsonPreferencesPersistorTest {
    static {
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'trace')
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
        preferencesPersistor.content = '{"com": {"acme": {"SampleModel": {"pstring": "string"}}}}'
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
        preferencesManager.save(model)
        preferencesPersistor.write(preferencesManager)

        // expect:
        def json = new JsonSlurperClassic().parseText(preferencesPersistor.content)
        assert 'STRING' == json.com.acme.SampleModel.pstring
        assert !json.com.acme.SampleModel.pboolean
        assert '12/12/2012' == json.com.acme.SampleModel.pdate
    }

    @Nonnull
    private List<Module> moduleOverrides() {
        [
            new AbstractTestingModule() {
                @Override
                protected void doConfigure() {
                    bind(PreferencesPersistor)
                        .to(InMemoryPreferencesPersistor)
                        .asSingleton()
                }
            }
        ]
    }

    private static class InMemoryPreferencesPersistor extends JsonPreferencesPersistor {
        String content = '{}'

        @Inject
        InMemoryPreferencesPersistor(@Nonnull GriffonApplication application,
                                     @Nonnull Metadata metadata,
                                     @Nonnull ConverterRegistry converterRegistry) {
            super(application, metadata, converterRegistry)
        }

        @Nonnull
        @Override
        protected InputStream inputStream() throws IOException {
            return new ByteArrayInputStream(content.getBytes())
        }

        @Nonnull
        @Override
        protected OutputStream outputStream() throws IOException {
            StringOutputStream os = new StringOutputStream()
            os.closeCallback = new StringOutputStream.CloseCallback() {
                @Override
                void closed(@Nonnull StringOutputStream stream) {
                    content = stream.toString()
                }
            }
            return os
        }
    }

    private static class StringOutputStream extends ByteArrayOutputStream {
        static interface CloseCallback {
            void closed(@Nonnull StringOutputStream stream)
        }

        CloseCallback closeCallback

        @Override
        void close() throws IOException {
            super.close()
            closeCallback?.closed(this)
        }
    }
}
