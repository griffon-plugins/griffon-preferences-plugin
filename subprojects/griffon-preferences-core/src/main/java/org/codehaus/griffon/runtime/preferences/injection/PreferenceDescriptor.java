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
package org.codehaus.griffon.runtime.preferences.injection;

import java.beans.PropertyEditor;

/**
 * @author Andres Almiray
 */
public abstract class PreferenceDescriptor {
    public final String fqName;
    public final String path;
    public final String[] args;
    public final String defaultValue;
    public final String format;
    public final Class<? extends PropertyEditor> editor;

    public PreferenceDescriptor(String fqName, String path, String[] args, String defaultValue, String format, Class<? extends PropertyEditor> editor) {
        this.fqName = fqName;
        this.path = path;
        this.args = args;
        this.defaultValue = defaultValue;
        this.format = format;
        this.editor = editor;
    }

    public abstract InjectionPoint asInjectionPoint();
}
