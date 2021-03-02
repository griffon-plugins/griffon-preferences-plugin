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
package org.codehaus.griffon.runtime.preferences;

import griffon.annotations.core.Nonnull;
import griffon.annotations.core.Nullable;
import griffon.core.CallableWithArgs;
import griffon.core.GriffonApplication;
import griffon.core.events.DestroyInstanceEvent;
import griffon.core.events.NewInstanceEvent;
import griffon.exceptions.GriffonException;
import griffon.plugins.preferences.KeyResolutionStrategy;
import griffon.plugins.preferences.NodeChangeEvent;
import griffon.plugins.preferences.Preference;
import griffon.plugins.preferences.PreferencesAware;
import griffon.plugins.preferences.PreferencesManager;
import griffon.plugins.preferences.PreferencesNode;
import griffon.util.GriffonClassUtils;
import org.codehaus.griffon.runtime.preferences.injection.FieldPreferenceDescriptor;
import org.codehaus.griffon.runtime.preferences.injection.InjectionPoint;
import org.codehaus.griffon.runtime.preferences.injection.InstanceContainer;
import org.codehaus.griffon.runtime.preferences.injection.InstanceStore;
import org.codehaus.griffon.runtime.preferences.injection.MethodPreferenceDescriptor;
import org.codehaus.griffon.runtime.preferences.injection.PreferenceDescriptor;
import org.kordamp.jsr377.converter.FormattingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.application.converter.Converter;
import javax.application.converter.ConverterRegistry;
import javax.application.converter.NoopConverter;
import javax.application.event.EventHandler;
import javax.inject.Inject;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static griffon.plugins.preferences.KeyResolutionStrategy.DECLARING_CLASS;
import static griffon.plugins.preferences.KeyResolutionStrategy.PREFERENCES_KEY_RESOLUTION_STRATEGY;
import static griffon.util.ConfigUtils.getConfigValueAsString;
import static griffon.util.GriffonNameUtils.isBlank;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public abstract class AbstractPreferencesManager implements PreferencesManager {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPreferencesManager.class);

    private static final String ERROR_INSTANCE_NULL = "Argument 'instance' must not be null";
    private static final String ERROR_TYPE_NULL = "Argument 'type' must not be null";
    private static final String ERROR_VALUE_NULL = "Argument 'value' must not be null";
    private static final String ERROR_EDITOR_CLASS_NULL = "Argument 'converter' must not be null";
    protected final InstanceStore instanceStore = new InstanceStore();
    protected final ConverterRegistry converterRegistry;

    @Inject
    protected GriffonApplication application;
    protected KeyResolutionStrategy keyResolutionStrategy;

    @Inject
    protected AbstractPreferencesManager(@Nonnull ConverterRegistry converterRegistry) {
        this.converterRegistry = requireNonNull(converterRegistry, "Argument 'converterRegistry' must not be null");
    }

    @PostConstruct
    private void initialize() {
        this.application = requireNonNull(application, "Argument 'application' must not be null");

        String resolutionStrategy = getConfigValueAsString(application.getConfiguration().asFlatMap(), PREFERENCES_KEY_RESOLUTION_STRATEGY, DECLARING_CLASS.name());
        try {
            keyResolutionStrategy = KeyResolutionStrategy.valueOf(resolutionStrategy.toUpperCase());
        } catch (Exception e) {
            keyResolutionStrategy = DECLARING_CLASS;
        }

        application.getEventRouter().subscribe(this);

        getPreferences().addNodeChangeListener(event -> {
            if (event.getType() == NodeChangeEvent.Type.ADDED) {
                for (InstanceContainer instanceContainer : instanceStore) {
                    if (instanceContainer.containsPartialPath(event.getPath())) {
                        injectPreferences(instanceContainer.instance());
                    }
                }
            }
        });

        getPreferences().addPreferencesChangeListener(event -> {
            for (InstanceContainer instanceContainer : instanceStore) {
                String path = event.getPath();
                if (PreferencesNode.PATH_SEPARATOR.equals(path)) {
                    path = event.getKey();
                } else {
                    path += "." + event.getKey();
                }
                if (instanceContainer.containsPath(path)) {
                    InjectionPoint injectionPoint = instanceContainer.getInjectionPoints().get(path);
                    Object value = event.getNewValue();

                    if (null != value) {
                        if (!injectionPoint.getType().isAssignableFrom(value.getClass())) {
                            value = convertValue(injectionPoint.getType(), value, injectionPoint.format, injectionPoint.converter);
                        }
                    }
                    injectionPoint.setValue(instanceContainer.instance(), value);
                }
            }
        });
    }

    @EventHandler
    public void handleNewInstanceEvent(@Nonnull NewInstanceEvent<?> event) {
        injectPreferences(event.getInstance());
    }

    @EventHandler
    public void handleDestroyInstanceEvent(@Nonnull DestroyInstanceEvent<?> event) {
        if (instanceStore.contains(event.getInstance())) {
            instanceStore.remove(event.getInstance());
        }
    }

    @Override
    public void save(@Nonnull Object instance) {
        requireNonNull(instance, ERROR_INSTANCE_NULL);

        Map<String, PreferenceDescriptor> descriptors = new LinkedHashMap<>();
        Class<?> klass = instance.getClass();
        do {
            harvestDescriptors(instance.getClass(), klass, instance, descriptors);
            klass = klass.getSuperclass();
        } while (null != klass);

        doSavePreferences(instance, descriptors);
    }

    @Override
    public void injectPreferences(@Nonnull Object instance) {
        requireNonNull(instance, ERROR_INSTANCE_NULL);

        Map<String, PreferenceDescriptor> descriptors = new LinkedHashMap<>();
        Class<?> klass = instance.getClass();
        do {
            harvestDescriptors(instance.getClass(), klass, instance, descriptors);
            klass = klass.getSuperclass();
        } while (null != klass);

        doPreferencesInjection(instance, descriptors);
        if (instance.getClass().getAnnotation(PreferencesAware.class) != null && !instanceStore.contains(instance)) {
            List<InjectionPoint> injectionPoints = new LinkedList<>();
            for (PreferenceDescriptor pd : descriptors.values()) {
                injectionPoints.add(pd.asInjectionPoint());
            }
            instanceStore.add(instance, injectionPoints);
        }
    }

    protected void harvestDescriptors(@Nonnull Class<?> instanceClass, @Nonnull Class<?> currentClass, @Nonnull Object instance, @Nonnull Map<String, PreferenceDescriptor> descriptors) {
        PropertyDescriptor[] propertyDescriptors = GriffonClassUtils.getPropertyDescriptors(currentClass);
        for (PropertyDescriptor pd : propertyDescriptors) {
            Method readMethod = pd.getReadMethod();
            Method writeMethod = pd.getWriteMethod();
            if (null == readMethod || null == writeMethod) {
                continue;
            }
            if (isStatic(readMethod.getModifiers()) || isStatic(writeMethod.getModifiers())) {
                continue;
            }

            Preference annotation = writeMethod.getAnnotation(Preference.class);
            if (null == annotation) {
                annotation = readMethod.getAnnotation(Preference.class);
            }
            if (null == annotation) {
                continue;
            }

            String propertyName = pd.getName();
            Class<?> resolvedClass = resolveClass(instanceClass, writeMethod.getDeclaringClass());
            String fqName = resolvedClass.getName().replace('$', '.') + "." + writeMethod.getName();
            String path = "/" + resolvedClass.getName().replace('$', '/').replace('.', '/') + "." + propertyName;
            String key = annotation.key();
            String[] args = annotation.args();
            String defaultValue = annotation.defaultValue();
            defaultValue = Preference.NO_VALUE.equals(defaultValue) ? null : defaultValue;
            String resolvedPath = !isBlank(key) ? key : path;
            String format = annotation.format();
            Class<? extends Converter<?>> converter = annotation.converter();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + propertyName +
                    " of instance " + instance +
                    " [path='" + resolvedPath +
                    "', args='" + Arrays.toString(args) +
                    "', defaultValue='" + defaultValue +
                    "', format='" + format +
                    "'] is marked for preference injection.");
            }
            descriptors.put(propertyName, new MethodPreferenceDescriptor(readMethod, writeMethod, fqName, resolvedPath, args, defaultValue, format, converter));
        }

        for (Field field : currentClass.getDeclaredFields()) {
            if (field.isSynthetic() || isStatic(field.getModifiers()) || descriptors.containsKey(field.getName())) {
                continue;
            }
            final Preference annotation = field.getAnnotation(Preference.class);
            if (null == annotation) {
                continue;
            }

            Class<?> resolvedClass = resolveClass(instanceClass, field.getDeclaringClass());
            String fqFieldName = resolvedClass.getName().replace('$', '.') + "." + field.getName();
            String path = "/" + resolvedClass.getName().replace('$', '/').replace('.', '/') + "." + field.getName();
            String key = annotation.key();
            String[] args = annotation.args();
            String defaultValue = annotation.defaultValue();
            defaultValue = Preference.NO_VALUE.equals(defaultValue) ? null : defaultValue;
            String resolvedPath = !isBlank(key) ? key : path;
            String format = annotation.format();
            Class<? extends Converter<?>> converter = annotation.converter();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Field " + fqFieldName +
                    " of instance " + instance +
                    " [path='" + resolvedPath +
                    "', args='" + Arrays.toString(args) +
                    "', defaultValue='" + defaultValue +
                    "', format='" + format +
                    "'] is marked for preference injection.");
            }

            descriptors.put(field.getName(), new FieldPreferenceDescriptor(field, fqFieldName, resolvedPath, args, defaultValue, format, converter));
        }
    }

    @Nonnull
    protected Class<?> resolveClass(@Nonnull Class<?> instanceClass, @Nonnull Class<?> declaringClass) {
        switch (keyResolutionStrategy) {
            case INSTANCE_CLASS:
                return instanceClass;
            case DECLARING_CLASS:
            default:
                return declaringClass;
        }
    }

    protected void doPreferencesInjection(@Nonnull Object instance, @Nonnull Map<String, PreferenceDescriptor> descriptors) {
        for (PreferenceDescriptor descriptor : descriptors.values()) {
            Object value = resolvePreference(descriptor.path, descriptor.args, descriptor.defaultValue);

            if (value != null) {
                InjectionPoint injectionPoint = descriptor.asInjectionPoint();
                if (!isNoopConverter(descriptor.converter) || !injectionPoint.getType().isAssignableFrom(value.getClass())) {
                    value = convertValue(injectionPoint.getType(), value, descriptor.format, descriptor.converter);
                }
                injectionPoint.setValue(instance, value);
            }
        }
    }

    protected void doSavePreferences(@Nonnull Object instance, @Nonnull Map<String, PreferenceDescriptor> descriptors) {
        for (PreferenceDescriptor descriptor : descriptors.values()) {
            InjectionPoint injectionPoint = descriptor.asInjectionPoint();
            Object value = injectionPoint.getValue(instance);
            String[] parsedPath = parsePath(descriptor.path);
            final PreferencesNode node = getPreferences().node(parsedPath[0]);
            final String key = parsedPath[1];

            if (value != null) {
                // Convert value only if descriptor.format is not null or there's a custom converter
                if (!isNoopConverter(descriptor.converter) || !isBlank(descriptor.format)) {
                    Converter converter = resolveConverter(value.getClass(), descriptor.format, descriptor.converter);
                    if (!isNoopConverter(converter.getClass())) {
                        value = converter.toString(value);
                    }
                }
                node.putAt(key, value);
            } else {
                node.remove(key);
            }
        }
    }

    @Nullable
    protected Object resolvePreference(@Nonnull String path, @Nonnull String[] args, @Nullable String defaultValue) {
        String[] parsedPath = parsePath(path);
        final PreferencesNode node = getPreferences().node(parsedPath[0]);
        final String key = parsedPath[1];

        if (node.containsKey(key)) {
            return evalPreferenceWithArguments(node.getAt(key), args);
        } else if (defaultValue != null) {
            node.putAt(key, defaultValue);
            return defaultValue;
        }
        return null;
    }

    @Nullable
    protected Object evalPreferenceWithArguments(@Nullable Object value, @Nullable Object[] args) {
        if (value instanceof CallableWithArgs) {
            CallableWithArgs callable = (CallableWithArgs) value;
            return callable.call(args);
        } else if (value instanceof CharSequence) {
            return formatPreferenceValue(String.valueOf(value), args);
        }
        return value;
    }

    @Nonnull
    protected String formatPreferenceValue(@Nonnull String message, @Nullable Object[] args) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Formatting message={} args={}", message, Arrays.toString(args));
        }
        if (args == null || args.length == 0) {
            return message;
        }
        return MessageFormat.format(message, args);
    }

    @Nonnull
    protected Object convertValue(@Nonnull Class<?> type, @Nonnull Object value, @Nullable String format, @Nonnull Class<? extends Converter<?>> converter) {
        requireNonNull(type, ERROR_TYPE_NULL);
        requireNonNull(value, ERROR_VALUE_NULL);

        Converter<?> resolvedConverter = resolveConverter(type, format, converter);
        if (isNoopConverter(resolvedConverter.getClass())) {
            return value;
        }

        return resolvedConverter.fromObject(value);
    }

    @Nonnull
    protected Converter<?> resolveConverter(@Nonnull Class<?> type, @Nullable String format, @Nonnull Class<? extends Converter<?>> converter) {
        requireNonNull(type, ERROR_TYPE_NULL);
        requireNonNull(converter, ERROR_EDITOR_CLASS_NULL);

        Converter<?> foundConverter = null;
        if (isNoopConverter(converter)) {
            foundConverter = converterRegistry.findConverter(type);
        } else {
            try {
                foundConverter = converter.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new GriffonException("Could not instantiate converter with " + converter, e);
            }
        }

        if (foundConverter instanceof FormattingConverter && !isBlank(format)) {
            ((FormattingConverter) foundConverter).setFormat(format);
        }
        return foundConverter;
    }

    @Nonnull
    protected String[] parsePath(@Nonnull String path) {
        int split = path.lastIndexOf(".");
        String head = split < 0 ? path : path.substring(0, split);
        String tail = split > 0 ? path.substring(split + 1) : null;
        head = head.replace('.', '/');
        return new String[]{head, tail};
    }

    protected boolean isNoopConverter(@Nonnull Class<?> converter) {
        return NoopConverter.class.isAssignableFrom(converter);
    }
}
