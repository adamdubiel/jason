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

import com.google.gson.reflect.TypeToken;

/**
 *
 * @author Adam Dubiel
 */
public class DefaultRuntimeFilters implements RuntimeFilters {

    private ExclusionFilter exclusionFilter = new EmptyExclusionFilter();

    private NamingFilter namingFilter = new EmptyNamingFilter();

    public DefaultRuntimeFilters() {
    }

    public DefaultRuntimeFilters(NamingFilter namingFilter) {
        this.namingFilter = namingFilter;
    }

    public DefaultRuntimeFilters(ExclusionFilter exclusionFilter) {
        this.exclusionFilter = exclusionFilter;
    }

    public DefaultRuntimeFilters(ExclusionFilter exclusionFilter, NamingFilter namingFilter) {
        this.exclusionFilter = exclusionFilter;
        this.namingFilter = namingFilter;
    }

    @Override
    public boolean skipField(TypeToken<?> declaringType, String fieldName, Object value) {
        return exclusionFilter.skipField(declaringType.getType(), fieldName, value);
    }

    @Override
    public boolean skipField(TypeToken<?> declaringType, String fieldName) {
        return exclusionFilter.skipField(declaringType.getType(), fieldName);
    }

    @Override
    public String renameField(TypeToken<?> declaringType, String fieldName) {
        return namingFilter.renameField(declaringType.getType(), fieldName);
    }

}
