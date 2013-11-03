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

import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 *
 * @author Adam Dubiel
 */
public class ClassFieldNameMapperTest {

    @Test
    public void shouldReturnDefinedMapping() {
        // given
        ClassFieldNameMapper mapper = new ClassFieldNameMapper();
        mapper.addMapping("old", "new");

        // when
        String mappingValue = mapper.renameField("old");

        // then
        assertThat(mappingValue).isEqualTo("new");
    }

    @Test
    public void shouldReturnSameStringIfNoMappingDefined() {
        // given
        ClassFieldNameMapper mapper = new ClassFieldNameMapper();
        String value = "old";

        // when
        String mappingValue = mapper.renameField(value);

        // then
        assertThat(mappingValue).isSameAs(value);
    }

}
