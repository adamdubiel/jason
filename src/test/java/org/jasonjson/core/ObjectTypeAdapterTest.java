/*
 * Copyright (C) 2011 Google Inc.
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

package org.jasonjson.core;

import org.jasonjson.core.JasonBuilder;
import org.jasonjson.core.Jason;
import org.jasonjson.core.TypeAdapter;
import org.jasonjson.core.filter.EmptyRuntimeFilters;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import junit.framework.TestCase;

public final class ObjectTypeAdapterTest extends TestCase {
  private final Jason gson = new JasonBuilder().create();
  private final TypeAdapter<Object> adapter = gson.getAdapter(Object.class);

  public void testDeserialize() throws Exception {
    Map<?, ?> map = (Map<?, ?>) adapter.fromJson("{\"a\":5,\"b\":[1,2,null],\"c\":{\"x\":\"y\"}}", new EmptyRuntimeFilters());
    assertEquals(5.0, map.get("a"));
    assertEquals(Arrays.asList(1.0, 2.0, null), map.get("b"));
    assertEquals(Collections.singletonMap("x", "y"), map.get("c"));
    assertEquals(3, map.size());
  }

  public void testSerialize() throws Exception {
    Object object = new RuntimeType();
    assertEquals("{'a':5,'b':[1,2,null]}", adapter.toJson(object, new EmptyRuntimeFilters()).replace("\"", "'"));
  }

  public void testSerializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", null);
    assertEquals("{'a':null}", adapter.toJson(map, new EmptyRuntimeFilters()).replace('"', '\''));
  }

  public void testDeserializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", null);
    assertEquals(map, adapter.fromJson("{\"a\":null}", new EmptyRuntimeFilters()));
  }

  public void testSerializeObject() throws Exception {
    assertEquals("{}", adapter.toJson(new Object(), new EmptyRuntimeFilters()));
  }

  @SuppressWarnings("unused")
  private class RuntimeType {
    Object a = 5;
    Object b = Arrays.asList(1, 2, null);
  }
}
