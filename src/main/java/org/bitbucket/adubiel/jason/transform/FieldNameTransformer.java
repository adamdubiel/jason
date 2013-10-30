/*
 * Copyright 2013 Adam Dubiel, Przemek Hertel.
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
package org.bitbucket.adubiel.jason.transform;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Adam Dubiel
 */
public class FieldNameTransformer {

    private final Map<Class<?>, Map<String, String>> nameTransformations = new HashMap<Class<?>, Map<String, String>>();

    public void transforming(Class<?> clazz, String name, String transformedName) {
        Map<String, String> classTransformations = nameTransformations.get(clazz);
        if (classTransformations == null) {
            classTransformations = new HashMap<String, String>();
            nameTransformations.put(clazz, classTransformations);
        }
        classTransformations.put(name, transformedName);
    }

    public String transformName(Class<?> clazz, String name) {
        Map<String, String> classTransformations = nameTransformations.get(clazz);
        if (classTransformations != null) {
            String transformedName = classTransformations.get(name);
            return transformedName != null ? transformedName : name;
        }
        return name;
    }
}
