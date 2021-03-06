/*
 * Copyright (C) 2012 Google Inc.
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
import org.jasonjson.core.stream.JsonReader;
import org.jasonjson.core.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import junit.framework.TestCase;

/**
 * @author Jesse Wilson
 */
public class OverrideCoreTypeAdaptersTest extends TestCase {
  private static final TypeAdapter<Boolean> booleanAsIntAdapter = new SimpleTypeAdapter<Boolean>() {
    @Override public void write(JsonWriter out, Boolean value) throws IOException {
      out.value(value ? 1 : 0);
    }
    @Override public Boolean read(JsonReader in) throws IOException {
      int value = in.nextInt();
      return value != 0;
    }
  };

  private static final TypeAdapter<String> swapCaseStringAdapter = new SimpleTypeAdapter<String>() {
    @Override public void write(JsonWriter out, String value) throws IOException {
      out.value(value.toUpperCase(Locale.US));
    }
    @Override public String read(JsonReader in) throws IOException {
      return in.nextString().toLowerCase(Locale.US);
    }
  };

  public void testOverrideWrapperBooleanAdapter() {
    Jason gson = new JasonBuilder()
        .registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
        .create();
    assertEquals("true", gson.toJson(true, boolean.class));
    assertEquals("1", gson.toJson(true, Boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("true", boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("1", Boolean.class));
    assertEquals(Boolean.FALSE, gson.fromJson("0", Boolean.class));
  }

  public void testOverridePrimitiveBooleanAdapter() {
    Jason gson = new JasonBuilder()
        .registerTypeAdapter(boolean.class, booleanAsIntAdapter)
        .create();
    assertEquals("1", gson.toJson(true, boolean.class));
    assertEquals("true", gson.toJson(true, Boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("1", boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("true", Boolean.class));
    assertEquals("0", gson.toJson(false, boolean.class));
  }

  public void testOverrideStringAdapter() {
    Jason gson = new JasonBuilder()
        .registerTypeAdapter(String.class, swapCaseStringAdapter)
        .create();
    assertEquals("\"HELLO\"", gson.toJson("Hello", String.class));
    assertEquals("hello", gson.fromJson("\"Hello\"", String.class));
  }
}
