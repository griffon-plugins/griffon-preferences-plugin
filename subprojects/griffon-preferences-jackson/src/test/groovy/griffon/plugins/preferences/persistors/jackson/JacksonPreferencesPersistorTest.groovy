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
package griffon.plugins.preferences.persistors.jackson

import com.acme.SampleModel
import com.fasterxml.jackson.databind.ObjectMapper
import griffon.annotations.core.Nonnull
import griffon.annotations.inject.DependsOn
import griffon.core.GriffonApplication
import griffon.core.env.Metadata
import griffon.core.injection.Module
import griffon.plugins.preferences.PreferencesManager
import griffon.plugins.preferences.PreferencesPersistor
import griffon.test.core.GriffonUnitRule
import org.codehaus.griffon.test.core.injection.AbstractTestingModule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.msgpack.jackson.dataformat.MessagePackFactory

import javax.application.converter.ConverterRegistry
import javax.inject.Inject
import javax.inject.Provider
import java.text.SimpleDateFormat

@DependsOn('preferences-jackson')
class JacksonPreferencesPersistorTest {
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

    @Inject
    private ObjectMapper objectMapper

    private Date january2K

    @Before
    void setup() {
        january2K = new SimpleDateFormat('dd/MM/yyyy').parse('01/01/2000')
        preferencesPersistor.content = objectMapper.writeValueAsBytes([com: [acme: [SampleModel: [pstring: 'string']]]])
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
        def map = objectMapper.readValue(preferencesPersistor.content, Map)
        assert 'STRING' == map.com.acme.SampleModel.pstring
        assert !map.com.acme.SampleModel.pboolean
        assert '12/12/2012' == map.com.acme.SampleModel.pdate
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
                    bind(ObjectMapper)
                        .toProvider(ObjectMapperProvider)
                        .asSingleton()
                }
            }
        ]
    }

    private static class InMemoryPreferencesPersistor extends JacksonPreferencesPersistor {
        byte[] content = [] as byte[]

        @Inject
        InMemoryPreferencesPersistor(@Nonnull GriffonApplication application,
                                     @Nonnull Metadata metadata,
                                     @Nonnull ConverterRegistry converterRegistry,
                                     @ Nonnull ObjectMapper objectMapper) {
            super(application, metadata, converterRegistry, objectMapper)
        }

        @Nonnull
        @Override
        protected InputStream inputStream() throws IOException {
            return new ByteArrayInputStream(content)
        }

        @Nonnull
        @Override
        protected OutputStream outputStream() throws IOException {
            StringOutputStream os = new StringOutputStream()
            os.closeCallback = new StringOutputStream.CloseCallback() {
                @Override
                void closed(@Nonnull StringOutputStream stream) {
                    content = stream.toByteArray()
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

    private static class ObjectMapperProvider implements Provider<ObjectMapper> {
        @Override
        ObjectMapper get() {
            ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory())
            objectMapper.registerSubtypes(SampleModel)
            return objectMapper
        }
    }
}
