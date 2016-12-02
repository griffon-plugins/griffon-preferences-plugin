/*
 * Copyright 2014-2016 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Andres Almiray
 */
public class MethodPreferenceDescriptor extends PreferenceDescriptor {
    public final Method readMethod;
    public final Method writeMethod;

    public MethodPreferenceDescriptor(Method readMethod, Method writeMethod, String fqName, String path, String[] args, String defaultValue, String format) {
        super(fqName, path, args, defaultValue, format);
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
    }

    public InjectionPoint asInjectionPoint() {
        return new MethodInjectionPoint(readMethod, writeMethod, fqName, path, format);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MethodPreferenceDescriptor{");
        sb.append("readMethod=").append(readMethod);
        sb.append(", writeMethod=").append(writeMethod);
        sb.append(", fqName='").append(fqName).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", args=").append(Arrays.toString(args));
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", format='").append(format).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
