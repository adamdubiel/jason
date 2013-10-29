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
package org.bitbucket.adubiel.jason.attribute;

import java.beans.PropertyDescriptor;
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

    private final PropertyDescriptor descriptor;

    private final Method readMethod;

    public PropertyAttribute(PropertyDescriptor descriptor) {
        this.descriptor = descriptor;
        this.readMethod = descriptor.getReadMethod();
    }

    public String getName() {
        return descriptor.getName();
    }

    public Class<?> getDeclaringClass() {
        return readMethod.getDeclaringClass();
    }

    public Type getDeclaredType() {
        return readMethod.getGenericReturnType();
    }

    public Class<?> getDeclaredClass() {
        return readMethod.getReturnType();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return readMethod.getAnnotation(annotation);
    }

    public Collection<Annotation> getAnnotations() {
        return Arrays.asList(readMethod.getDeclaredAnnotations());
    }

    public boolean hasModifier(int modifier) {
        return (readMethod.getModifiers() & modifier) != 0;
    }

    public int getModifiers() {
        return readMethod.getModifiers();
    }

    public Object get(Object instance) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException {
        return readMethod.invoke(instance);
    }

    public void set(Object instance, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        descriptor.getWriteMethod().invoke(instance, value);
    }

    public boolean isSynthetic() {
        return readMethod.isSynthetic();
    }

}
