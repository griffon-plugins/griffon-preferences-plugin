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
package griffon.plugins.preferences;

import griffon.annotations.core.Nonnull;

import java.io.Serializable;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class NodeChangeEvent implements Serializable {
    private static final long serialVersionUID = -1751843471998802294L;

    public static enum Type {
        ADDED, REMOVED
    }

    private final String path;
    private final Type type;

    public NodeChangeEvent(@Nonnull String path, @Nonnull Type type) {
        this.path = requireNonBlank(path, "Argument 'path' must not be blank");
        this.type = requireNonNull(type, "Argument 'type' must not be null");
    }

    @Nonnull
    public String getPath() {
        return path;
    }

    @Nonnull
    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeChangeEvent that = (NodeChangeEvent) o;

        return path.equals(that.path) && type == that.type;
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NodeChangeEvent{" +
            "path='" + path + '\'' +
            ", type=" + type +
            '}';
    }
}
