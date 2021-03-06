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

import griffon.exceptions.InstanceMethodInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.application.converter.Converter;
import java.lang.reflect.Field;

import static griffon.core.GriffonExceptionHandler.sanitize;
import static griffon.util.GriffonClassUtils.invokeExactInstanceMethod;
import static griffon.util.GriffonNameUtils.getGetterName;
import static griffon.util.GriffonNameUtils.getSetterName;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class FieldInjectionPoint extends InjectionPoint {
    private static final Logger LOG = LoggerFactory.getLogger(FieldInjectionPoint.class);

    private static final String ERROR_INSTANCE_NULL = "Argument 'instance' must not be null";
    private static final String ERROR_FIELD_NULL = "Argument 'field' must not be null";
    private static final String CANNOT_SET_VALUE_ON_FIELD_OF_INSTANCE = "Cannot set value on field {} of instance {}";

    public final Field field;

    public FieldInjectionPoint(Field field, String fqName, String path, String format, Class<? extends Converter<?>> converter) {
        super(fqName, path, format, converter);
        this.field = field;
    }

    public void setValue(Object instance, Object value) {
        requireNonNull(instance, ERROR_INSTANCE_NULL);
        requireNonNull(field, ERROR_FIELD_NULL);
        String setter = getSetterName(field.getName());
        try {
            invokeExactInstanceMethod(instance, setter, value);
        } catch (InstanceMethodInvocationException imie) {
            try {
                field.setAccessible(true);
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                LOG.warn(CANNOT_SET_VALUE_ON_FIELD_OF_INSTANCE, fqName, instance, sanitize(e));
            }
        }
    }

    public Object getValue(Object instance) {
        String getter = getGetterName(field.getName());
        try {
            return invokeExactInstanceMethod(instance, getter);
        } catch (InstanceMethodInvocationException imie) {
            try {
                field.setAccessible(true);
                return field.get(instance);
            } catch (IllegalAccessException e) {
                LOG.warn(CANNOT_SET_VALUE_ON_FIELD_OF_INSTANCE, fqName, instance, sanitize(e));
            }
        }
        return null;
    }

    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FieldInjectionPoint{");
        sb.append("field=").append(field);
        sb.append(", fqName='").append(fqName).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", format='").append(format).append('\'');
        sb.append(", converter='").append(converter).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
