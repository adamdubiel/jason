/*
 * Copyright 2013 Adam Dubiel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasonjson.core.attribute;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Adam Dubiel
 */
public class PropertyAttribute implements Attribute {

    private final String name;

    private final Method method;

    public PropertyAttribute(Method method, String name) {
        this.method = method;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }

    public Type getDeclaredType() {
        return method.getGenericReturnType();
    }

    public Class<?> getDeclaredClass() {
        return method.getReturnType();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return method.getAnnotation(annotation);
    }

    public Collection<Annotation> getAnnotations() {
        return Arrays.asList(method.getDeclaredAnnotations());
    }

    public boolean hasModifier(int modifier) {
        return (method.getModifiers() & modifier) != 0;
    }

    public int getModifiers() {
        return method.getModifiers();
    }

    public Object get(Object instance) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException {
        return method.invoke(instance);
    }

    public void set(Object instance, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        throw new UnsupportedOperationException("Jason does not support setter deserialization (yet)!");
    }

    public boolean isSynthetic() {
        return method.isSynthetic();
    }

}
