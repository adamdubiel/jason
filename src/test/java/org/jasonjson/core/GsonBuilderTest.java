/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.jasonjson.core.SimpleTypeAdapter;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import junit.framework.TestCase;

import org.jasonjson.core.stream.JsonReader;
import org.jasonjson.core.stream.JsonWriter;

/**
 * Unit tests for {@link JasonBuilder}.
 *
 * @author Inderjeet Singh
 */
public class GsonBuilderTest extends TestCase {
  private static final TypeAdapter<Object> NULL_TYPE_ADAPTER = new SimpleTypeAdapter<Object>() {
    @Override public void write(JsonWriter out, Object value) {
      throw new AssertionError();
    }
    @Override public Object read(JsonReader in) {
      throw new AssertionError();
    }
  };

  public void testCreatingMoreThanOnce() {
    JasonBuilder builder = new JasonBuilder();
    builder.create();
    builder.create();
  }

  public void testExcludeFieldsWithModifiers() {
    Jason gson = new JasonBuilder()
        .excludeFieldsWithModifiers(Modifier.VOLATILE, Modifier.PRIVATE)
        .create();
    assertEquals("{\"d\":\"d\"}", gson.toJson(new HasModifiers()));
  }

  public void testRegisterTypeAdapterForCoreType() {
    Type[] types = {
        byte.class,
        int.class,
        double.class,
        Short.class,
        Long.class,
        String.class,
    };
    for (Type type : types) {
      new JasonBuilder().registerTypeAdapter(type, NULL_TYPE_ADAPTER);
    }
  }

  @SuppressWarnings("unused")
  static class HasModifiers {
    private String a = "a";
    volatile String b = "b";
    private volatile String c = "c";
    String d = "d";
  }

  public void testTransientFieldExclusion() {
    Jason gson = new JasonBuilder()
        .excludeFieldsWithModifiers()
        .create();
    assertEquals("{\"a\":\"a\"}", gson.toJson(new HasTransients()));
  }

  static class HasTransients {
    transient String a = "a";
  }
}
