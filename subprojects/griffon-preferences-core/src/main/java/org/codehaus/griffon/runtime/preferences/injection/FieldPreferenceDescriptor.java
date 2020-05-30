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
package org.codehaus.griffon.runtime.preferences.injection;

import javax.application.converter.Converter;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author Andres Almiray
 */
public class FieldPreferenceDescriptor extends PreferenceDescriptor {
    public final Field field;

    public FieldPreferenceDescriptor(Field field, String fqName, String path, String[] args, String defaultValue, String format, Class<? extends Converter<?>> converter) {
        super(fqName, path, args, defaultValue, format, converter);
        this.field = field;
    }

    public InjectionPoint asInjectionPoint() {
        return new FieldInjectionPoint(field, fqName, path, format, converter);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FieldPreferenceDescriptor{");
        sb.append("field=").append(field);
        sb.append(", fqName='").append(fqName).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", args=").append(Arrays.toString(args));
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", format='").append(format).append('\'');
        sb.append(", converter='").append(converter).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
