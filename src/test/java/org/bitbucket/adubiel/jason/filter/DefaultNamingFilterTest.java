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
import static org.fest.assertions.api.Assertions.assertThat;

/**
 *
 * @author Adam Dubiel
 */
public class DefaultNamingFilterTest {

    @Test
    public void shouldReturnNewNameForFieldIfDefined() {
        // given
        DefaultNamingFilter nameFilter = new DefaultNamingFilter().rename(Object.class, "oldName", "new_name");

        // when
        String newName = nameFilter.renameField(Object.class, "oldName");

        // then
        assertThat(newName).isEqualTo("new_name");
    }

    @Test
    public void shouldStoreMultipleRenamesPerClass() {
        // given
        DefaultNamingFilter nameFilter = new DefaultNamingFilter().rename(Object.class, "oldName", "new_name")
                .rename(Object.class, "otherOldName", "other_new_name");

        // when
        String newName = nameFilter.renameField(Object.class, "oldName");
        String newOtherName = nameFilter.renameField(Object.class, "otherOldName");

        // then
        assertThat(newName).isEqualTo("new_name");
        assertThat(newOtherName).isEqualTo("other_new_name");
    }

    @Test
    public void shouldReturnSameStringIfNoRenameDefined() {
        // given
        DefaultNamingFilter nameFilter = new DefaultNamingFilter();
        String oldName = "undefined";

        // when
        String newName = nameFilter.renameField(Object.class, oldName);

        // then
        assertThat(newName).isSameAs(oldName);
    }

}
