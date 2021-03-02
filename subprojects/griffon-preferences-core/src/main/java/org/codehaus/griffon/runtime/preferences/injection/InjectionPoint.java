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
package org.codehaus.griffon.runtime.preferences.injection;

import javax.application.converter.Converter;

/**
 * @author Andres Almiray
 */
public abstract class InjectionPoint {
    public final String fqName;
    public final String path;
    public final String format;
    public final Class<? extends Converter<?>> converter;

    public InjectionPoint(String fqName, String path, String format, Class<? extends Converter<?>> converter) {
        this.fqName = fqName;
        this.path = path;
        this.format = format;
        this.converter = converter;
    }

    public abstract void setValue(Object instance, Object value);

    public abstract Object getValue(Object instance);

    public abstract Class<?> getType();
}
