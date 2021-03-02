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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.application.converter.Converter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static griffon.core.GriffonExceptionHandler.sanitize;

/**
 * @author Andres Almiray
 */
public class MethodInjectionPoint extends InjectionPoint {
    private static final Logger LOG = LoggerFactory.getLogger(FieldInjectionPoint.class);
    private static final String CANNOT_GET_VALUE_ON_METHOD_OF_INSTANCE = "Cannot get value on method {}() of instance {}";

    public final Method readMethod;
    public final Method writeMethod;
    public final Class<?> type;

    public MethodInjectionPoint(Method readMethod, Method writeMethod, String fqName, String path, String format, Class<? extends Converter<?>> converter) {
        super(fqName, path, format, converter);
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.type = readMethod.getReturnType();
    }

    public void setValue(Object instance, Object value) {
        try {
            writeMethod.invoke(instance, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(CANNOT_GET_VALUE_ON_METHOD_OF_INSTANCE, fqName, instance, sanitize(e));
            }
        }
    }

    public Object getValue(Object instance) {
        try {
            return readMethod.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(CANNOT_GET_VALUE_ON_METHOD_OF_INSTANCE, fqName, instance, sanitize(e));
            }
        }
        return null;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MethodInjectionPoint{");
        sb.append("readMethod=").append(readMethod);
        sb.append(", writeMethod=").append(writeMethod);
        sb.append(", type=").append(type);
        sb.append(", fqName='").append(fqName).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", format='").append(format).append('\'');
        sb.append(", converter='").append(converter).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
