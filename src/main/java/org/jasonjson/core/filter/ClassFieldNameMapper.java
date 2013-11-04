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
package org.jasonjson.core.filter;

import java.util.HashMap;

/**
 *
 * @author Adam Dubiel
 */
public class ClassFieldNameMapper {

    private final HashMap<String, String> nameMapping = new HashMap<String, String>();

    ClassFieldNameMapper() {
    }

    void addMapping(String from, String to) {
        nameMapping.put(from, to);
    }

    String renameField(String fieldName) {
        String renamedField = nameMapping.get(fieldName);
        if (renamedField != null) {
            return renamedField;
        }
        return fieldName;
    }
}
