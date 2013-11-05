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
import com.jayway.jsonassert.JsonAssert;
import org.jasonsjon.core.test.model.PropertyAccessedParent;
import org.junit.Test;

/**
 *
 * @author Adam Dubiel
 */
public class PropertiesAccessTest {

    @Test
    public void shouldFetchPropertyUsingGetter() {
        // given
        Jason gson = new JasonBuilder().create();
        PropertyAccessedParent parent = new PropertyAccessedParent("getter name");

        // when
        String json = gson.toJson(parent);

        // then
        JsonAssert.with(json).assertEquals("$.getterName", "getter name").assertNotDefined("$.name");
    }
}
