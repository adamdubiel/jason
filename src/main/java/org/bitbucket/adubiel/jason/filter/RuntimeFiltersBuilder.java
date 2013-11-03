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
package org.bitbucket.adubiel.jason.filter;

import java.lang.reflect.Type;

/**
 *
 * @author Adam Dubiel
 */
public final class RuntimeFiltersBuilder {

    private AttributeExclusionFilter attributeExclusionFilter;

    private boolean emptyExclusionFilter = true;

    private DefaultNamingFilter defautNamingFilter;

    private boolean emptyNamingFilter = true;

    public RuntimeFiltersBuilder() {
    }

    public static RuntimeFiltersBuilder runtimeFilters() {
        return new RuntimeFiltersBuilder();
    }

    public RuntimeFilters build() {
        if(emptyExclusionFilter && emptyNamingFilter) {
            return new EmptyRuntimeFilters();
        }

        ExclusionFilter exclusionFilter = emptyExclusionFilter ? new EmptyExclusionFilter() : attributeExclusionFilter;
        NamingFilter namingFilter = emptyNamingFilter ? new EmptyNamingFilter() : defautNamingFilter;

        return new DefaultRuntimeFilters(exclusionFilter, namingFilter);
    }

    private AttributeExclusionFilter getExclusionFilter() {
        if (emptyExclusionFilter) {
            attributeExclusionFilter = new AttributeExclusionFilter();
            emptyExclusionFilter = false;
        }
        return attributeExclusionFilter;
    }

    private DefaultNamingFilter getNamingFilter() {
        if (emptyNamingFilter) {
            defautNamingFilter = new DefaultNamingFilter();
            emptyNamingFilter = false;
        }
        return defautNamingFilter;
    }

    public RuntimeFiltersBuilder including(String... globalIncludes) {
        getExclusionFilter().including(globalIncludes);
        return this;
    }

    public RuntimeFiltersBuilder excluding(String... globalExcludes) {
        getExclusionFilter().excluding(globalExcludes);
        return this;
    }

    public RuntimeFiltersBuilder including(Class<?> targetClass, String... includes) {
        getExclusionFilter().including(targetClass, includes);
        return this;
    }

    public RuntimeFiltersBuilder excluding(Class<?> targetClass, String... excludes) {
        getExclusionFilter().excluding(targetClass, excludes);
        return this;
    }

    public RuntimeFiltersBuilder rename(Type type, String from, String to) {
        getNamingFilter().rename(type, from, to);
        return this;
    }
}
