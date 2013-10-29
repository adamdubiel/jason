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

import com.google.gson.reflect.TypeToken;
import org.bitbucket.adubiel.jason.filter.AttributeFilter;

/**
 *
 * @author Adam Dubiel
 */
public class DefaultRuntimeTransformer implements RuntimeTransformer {

    private AttributeFilter attributeFilters;

    private FieldNameTransformer fieldNameTransformer;

    public DefaultRuntimeTransformer(FieldNameTransformer fieldNameTransformer) {
        this.fieldNameTransformer = fieldNameTransformer;
    }

    public DefaultRuntimeTransformer(AttributeFilter attributeFilters) {
        this.attributeFilters = attributeFilters;
    }

    public boolean skipField(TypeToken<?> token, String fieldName) {
        return !attributeFilters.allow(token.getRawType(), fieldName);
    }

    public String transformName(TypeToken<?> token, String fieldName) {
        return fieldNameTransformer != null ? fieldNameTransformer.transformName(token.getRawType(), fieldName) : fieldName;
    }

    public <T> Object transformValue(TypeToken<?> token, String fieldName, T originalValue) {
        return originalValue;
    }
}
