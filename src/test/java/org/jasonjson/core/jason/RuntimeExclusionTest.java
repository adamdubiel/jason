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
package org.jasonjson.core.jason;

import org.jasonjson.core.Jason;
import org.jasonjson.core.JasonBuilder;
import org.jasonjson.core.filter.RuntimeFilters;
import com.jayway.jsonassert.JsonAssert;
import org.jasonjson.core.filter.RuntimeFiltersBuilder;
import org.jasonsjon.core.test.model.Parent;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 *
 * @author Adam Dubiel
 */
public class RuntimeExclusionTest {

    @Test
    public void shouldSkipNameFieldSpecifiedInRuntimeFiltersOnSerialization() {
        // given
        Jason gson = new JasonBuilder().create();
        Parent parent = new Parent(1, "should be skipped");

        RuntimeFilters filters = RuntimeFiltersBuilder.runtimeFilters().including(Parent.class, "id").build();

        // when
        String json = gson.toJson(parent, filters);

        // then
        JsonAssert.with(json).assertEquals("$.id", 1).assertNotDefined("$.name");
    }

    @Test
    public void shouldSkipNameFieldSpecifiedInRuntimeFiltersOnDeserialization() {
        // given
        Jason gson = new JasonBuilder().create();
        String parentJson = "{ id: 1, name: \"should be skipped\" }";

        RuntimeFilters filters = RuntimeFiltersBuilder.runtimeFilters().including(Parent.class, "id").build();

        // when
        Parent parent = gson.fromJson(parentJson, Parent.class, filters);

        // then
        assertThat(parent.getId()).isEqualTo(1);
        assertThat(parent.getName()).isNull();
    }

}
