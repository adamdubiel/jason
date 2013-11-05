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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jasonjson.core.AccessStrategyType;
import org.jasonjson.core.JasonAccess;
import org.junit.Test;

/**
 *
 * @author Adam Dubiel
 */
public class PropertiesAccessTest {

    @Test
    public void shouldFetchPropertyUsingGetter() {
        // given
        Jason jason = new JasonBuilder().create();
        InstanceClass instance = new InstanceClass("parentName", "instanceName");

        // when
        String json = jason.toJson(instance);

        // then
        JsonAssert.with(json).assertEquals("$.parentName", "parentName").assertEquals("$.firstName", "instanceName").assertNotDefined("$.name");
    }

    @Test
    public void shouldFetchPropertiesFromRuntimeType() {
        // given
        Jason jason = new JasonBuilder().create();
        List<AbstractParentClass> list = new ArrayList<AbstractParentClass>(Arrays.asList(new InstanceClass("parentName", "instanceName")));
        InstanceListWrapper wrapper = new InstanceListWrapper(list);

        // when
        String json = jason.toJson(wrapper);

        // then
        JsonAssert.with(json).assertEquals("$.list[0].parentName", "parentName").assertEquals("$.list[0].firstName", "instanceName").assertNotDefined("$.list[0].name");
    }

    @Test
    public void shouldSerializeGenericEnumsUsingName() {
        // given
        Jason jason = new JasonBuilder().create();
        EnumHolder holder = new EnumHolder(TestEnum.VALUE);

        // when
        String json = jason.toJson(holder);

        // then
        JsonAssert.with(json).assertEquals("$.value", "VALUE");
    }

    private static enum TestEnum {

        VALUE
    }

    private static class EnumHolder {

        Enum value;

        EnumHolder(Enum value) {
            this.value = value;
        }
    }

    @JasonAccess(strategy = AccessStrategyType.PROPERTY)
    private static class InstanceListWrapper {

        List<AbstractParentClass> list;

        InstanceListWrapper(List<AbstractParentClass> list) {
            this.list = list;
        }

        List<AbstractParentClass> getList() {
            return list;
        }
    }

    @JasonAccess(strategy = AccessStrategyType.PROPERTY)
    private abstract static class AbstractParentClass {

        String name;

        String getParentName() {
            return name;
        }
    }

    @JasonAccess(strategy = AccessStrategyType.PROPERTY)
    private static class InstanceClass extends AbstractParentClass {

        String firstName;

        InstanceClass(String name, String firstName) {
            this.name = name;
            this.firstName = firstName;
        }

        String getFirstName() {
            return firstName;
        }
    }
}
