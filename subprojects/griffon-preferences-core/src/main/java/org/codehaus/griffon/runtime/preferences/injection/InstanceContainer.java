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

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public class InstanceContainer {
    protected final WeakReference<Object> instance;
    protected final Map<String, InjectionPoint> injectionPoints = new LinkedHashMap<String, InjectionPoint>();

    public InstanceContainer(Object instance, List<InjectionPoint> injectionPoints) {
        this.instance = new WeakReference<>(instance);
        for (InjectionPoint ip : injectionPoints) {
            this.injectionPoints.put(ip.path, ip);
        }
    }

    public Map<String, InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }

    public Object instance() {
        return instance.get();
    }

    public boolean containsPath(String path) {
        for (String p : injectionPoints.keySet()) {
            if (p.equals(path)) { return true; }
        }
        return false;
    }

    public boolean containsPartialPath(String path) {
        for (String p : injectionPoints.keySet()) {
            if (p.startsWith(path + ".")) { return true; }
        }
        return false;
    }
}
