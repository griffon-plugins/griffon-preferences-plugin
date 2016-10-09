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
package griffon.plugins.preferences;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public interface PreferencesNode {
    String PATH_SEPARATOR = "/";

    @Nonnull
    String name();

    @Nonnull
    String path();

    @Nullable
    PreferencesNode parent();

    /**
     * Returns the value associated with the given key.
     *
     * @param key the key to search
     * @return the value associated with the key or <tt>null<</tt> if not found.
     */
    @Nullable
    Object getAt(@Nonnull String key);

    /**
     * Returns the value associated with the given key.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key was not found
     * @return returns the value associated with the key, <tt>defaultValue</tt> if the key was not found
     */
    @Nullable
    Object getAt(@Nonnull String key, @Nullable Object defaultValue);

    /**
     * Returns the value associated with the given key.
     *
     * @param key the key to search
     * @param <T> the type of the value
     * @return returns the value associated with the key, <tt>defaultValue</tt> if the key was not found
     * @since 1.3.0o
     */
    @Nullable
    <T> T getAs(@Nonnull String key);

    /**
     * Returns the value associated with the given key.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key was not found
     * @param <T>          the type of the value
     * @return returns the value associated with the key, <tt>defaultValue</tt> if the key was not found
     * @since 1.3.0
     */
    @Nullable
    <T> T getAs(@Nonnull String key, @Nullable T defaultValue);

    /**
     * Finds a value associated with the given key. The value is
     * converted to type <tt>T</tt> if found using a {@code PropertyEditor}.
     *
     * @param key  the key to search
     * @param type the type to be returned
     * @since 1.3.0
     */
    @Nullable
    <T> T getConverted(@Nonnull String key, @Nonnull Class<T> type);

    /**
     * Finds a value associated with the given key. The value is
     * converted to type <tt>T</tt> if found using a {@code PropertyEditor}.
     * If not found then the supplied <tt>defaultValue</tt> will be returned.
     *
     * @param key          the key to search
     * @param type         the type to be returned
     * @param defaultValue the value to be returned if the key is not found
     * @since 1.3.0
     */
    @Nullable
    <T> T getConverted(@Nonnull String key, @Nonnull Class<T> type, @Nullable T defaultValue);

    void putAt(@Nonnull String key, @Nullable Object value);

    boolean isRoot();

    void remove(@Nonnull String key);

    void clear();

    @Nonnull
    String[] keys();

    boolean containsNode(@Nonnull Class<?> clazz);

    boolean containsNode(@Nonnull String path);

    boolean containsKey(@Nonnull String key);

    @Nonnull
    Map<String, PreferencesNode> children();

    @Nullable
    PreferencesNode node(@Nonnull Class<?> clazz);

    @Nullable
    PreferencesNode node(@Nonnull String path);

    @Nullable
    PreferencesNode removeNode(@Nonnull Class<?> clazz);

    @Nullable
    PreferencesNode removeNode(@Nonnull String path);

    @Nullable
    PreferencesNode getChildNode(@Nonnull String nodeName);

    @Nonnull
    PreferencesNode createChildNode(@Nonnull String nodeName);

    void storeChildNode(@Nonnull String nodeName, @Nonnull PreferencesNode node);

    @Nullable
    PreferencesNode removeChildNode(@Nonnull String nodeName);

    @Nonnull
    PreferencesNode merge(@Nonnull PreferencesNode other);
}
