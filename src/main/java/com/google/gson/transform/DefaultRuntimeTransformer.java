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
package com.google.gson.transform;

import com.google.gson.reflect.TypeToken;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Adam Dubiel
 */
public class DefaultRuntimeTransformer implements RuntimeTransformer {

    private Map<Class<?>, Set<String>> includedFields = new HashMap<Class<?>, Set<String>>();

    private FieldNameTransformer fieldNameTransformer;

    public DefaultRuntimeTransformer(FieldNameTransformer fieldNameTransformer) {
        this.fieldNameTransformer = fieldNameTransformer;
    }

    public DefaultRuntimeTransformer(Map<Class<?>, Set<String>> includedFields) {
        this.includedFields = includedFields;
    }

    public boolean skipField(TypeToken<?> token, String fieldName) {
        Set<String> fieldsForClass = includedFields.get(token.getRawType());
        return fieldsForClass == null || !fieldsForClass.contains(fieldName);
    }

    public String transformName(TypeToken<?> token, String fieldName) {
        return fieldNameTransformer != null ? fieldNameTransformer.transformName(token.getRawType(), fieldName) : fieldName;
    }

    public <T> Object transformValue(TypeToken<?> token, String fieldName, T originalValue) {
        return originalValue;
    }
}
