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
package org.jasonjson.core.bind;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jasonjson.core.AccessStrategyType;
import org.jasonjson.core.attribute.Attribute;
import org.jasonjson.core.attribute.FieldAttribute;
import org.jasonjson.core.attribute.PropertyAttribute;

/**
 *
 * @author Adam Dubiel
 */
final class AttributesExtractor {

    private AttributesExtractor() {
    }

    static Collection<Attribute> extractAttributes(Class<?> clazz, AccessStrategyType strategyType) {
        if (strategyType == AccessStrategyType.PROPERTY) {
            return extractFromProperties(clazz);
        } else {
            return extractFromFields(clazz);
        }
    }

    static Collection<Attribute> extractFromFields(Class<?> clazz) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            attributes.add(new FieldAttribute(field));
        }
        return attributes;
    }

    static Collection<Attribute> extractFromProperties(Class<?> clazz) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (isGetter(method)) {
                method.setAccessible(true);
                attributes.add(new PropertyAttribute(method, extractPropertyName(method)));
            }
        }
        return attributes;
    }

    private static boolean isGetter(Method method) {
        return method.getName().startsWith("get") || method.getName().startsWith("is");
    }

    private static String extractPropertyName(Method method) {
        String name = method.getName();
        name = name.replaceFirst("get|is", "");
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

}
