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

import org.jasonjson.core.internal.$Gson$Preconditions;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Adam Dubiel
 */
public class FieldAttribute implements Attribute {

    private final Field field;

    public FieldAttribute(Field f) {
        $Gson$Preconditions.checkNotNull(f);
        this.field = f;
    }

    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }

    public String getName() {
        return field.getName();
    }

    public Type getDeclaredType() {
        return field.getGenericType();
    }

    public Class<?> getDeclaredClass() {
        return field.getType();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return field.getAnnotation(annotation);
    }

    public Collection<Annotation> getAnnotations() {
        return Arrays.asList(field.getAnnotations());
    }

    public boolean hasModifier(int modifier) {
        return (field.getModifiers() & modifier) != 0;
    }

    public int getModifiers() {
        return field.getModifiers();
    }

    public Object get(Object instance) throws IllegalAccessException {
        return field.get(instance);
    }

    public void set(Object instance, Object value) throws IllegalAccessException {
        field.set(instance, value);
    }

    public boolean isSynthetic() {
        return field.isSynthetic();
    }
}
