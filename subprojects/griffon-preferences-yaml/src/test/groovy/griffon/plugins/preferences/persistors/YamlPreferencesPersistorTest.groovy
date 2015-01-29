/*
 * Copyright 2014-2015 the original author or authors.
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
package griffon.plugins.preferences.persistors

import com.acme.SampleModel
import griffon.core.GriffonApplication
import griffon.core.injection.Module
import griffon.core.test.GriffonUnitRule
import griffon.inject.DependsOn
import griffon.plugins.preferences.PreferencesManager
import griffon.plugins.preferences.PreferencesPersistor
import org.codehaus.griffon.runtime.core.injection.AbstractTestingModule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import javax.annotation.Nonnull
import javax.inject.Inject
import java.text.SimpleDateFormat

@DependsOn('preferences-yaml')
class YamlPreferencesPersistorTest {
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
    public void setup() {
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
        def yaml = new Yaml().load(preferencesPersistor.content)
        assert 'STRING' == yaml.com.acme.SampleModel.pstring
        assert !yaml.com.acme.SampleModel.pboolean
        assert '12/12/2012' == yaml.com.acme.SampleModel.pdate
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

    private static class InMemoryPreferencesPersistor extends YamlPreferencesPersistor {
        String content = '{}'

        @Inject
        InMemoryPreferencesPersistor(@Nonnull GriffonApplication application) {
            super(application)
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
