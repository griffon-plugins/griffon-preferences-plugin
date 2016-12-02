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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Andres Almiray
 */
public class InstanceStore implements Iterable<InstanceContainer> {
    protected final List<InstanceContainer> instances = new CopyOnWriteArrayList<InstanceContainer>();

    public void add(Object instance, List<InjectionPoint> injectionPoints) {
        if (null == instance) { return; }
        instances.add(new InstanceContainer(instance, injectionPoints));
    }

    public void remove(Object instance) {
        if (null == instance) { return; }
        InstanceContainer subject = null;
        for (InstanceContainer instance1 : instances) {
            subject = instance1;
            Object candidate = subject.instance();
            if (instance.equals(candidate)) {
                break;
            }
        }
        if (subject != null) { instances.remove(subject); }
    }

    public boolean contains(Object instance) {
        if (null == instance) { return false; }
        for (InstanceContainer instanceContainer : instances) {
            Object candidate = instanceContainer.instance();
            if (instance.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    public Iterator<InstanceContainer> iterator() {
        final Iterator<InstanceContainer> it = instances.iterator();
        return new Iterator<InstanceContainer>() {
            public boolean hasNext() {
                return it.hasNext();
            }

            public InstanceContainer next() {
                return it.next();
            }

            public void remove() {
                it.remove();
            }
        };
    }
}
