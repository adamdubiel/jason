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
package org.bitbucket.adubiel.jason.bind;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bitbucket.adubiel.jason.AccessStrategyType;
import org.bitbucket.adubiel.jason.attribute.Attribute;
import org.bitbucket.adubiel.jason.attribute.FieldAttribute;
import org.bitbucket.adubiel.jason.attribute.PropertyAttribute;

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
        try {
            for (PropertyDescriptor property : Introspector.getBeanInfo(clazz, clazz.getSuperclass()).getPropertyDescriptors()) {
                attributes.add(new PropertyAttribute(property));
            }
        } catch (IntrospectionException e) {
            throw new AssertionError(e);
        }
        return attributes;
    }

}
