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
package org.bitbucket.adubiel.jason;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bitbucket.adubiel.jason.transform.DefaultRuntimeTransformer;
import org.bitbucket.adubiel.jason.transform.RuntimeTransformer;
import com.jayway.jsonassert.JsonAssert;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bitbucket.adubiel.jason.test.model.Parent;
import org.junit.Test;

/**
 *
 * @author Adam Dubiel
 */
public class RuntimeExclusionTest {

    @Test
    public void shouldSkipNickNameFieldSpecifiedInExclusionStrategy() {
        // given
        Gson gson = new GsonBuilder().create();
        Parent parent = new Parent("test", "should be skipped");

        Map<Class<?>, Set<String>> includes = new HashMap<Class<?>, Set<String>>();
        includes.put(Parent.class, new HashSet<String>());
        includes.get(Parent.class).add("name");

        RuntimeTransformer transformer = new DefaultRuntimeTransformer(includes);

        // when
        String json = gson.toJson(parent, transformer);

        // then
        JsonAssert.with(json).assertEquals("$.name", "test").assertNotDefined("$.nickName");
    }

}
