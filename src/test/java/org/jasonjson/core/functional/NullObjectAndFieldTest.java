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

package org.jasonjson.core.functional;

import org.jasonjson.core.Jason;
import org.jasonjson.core.JasonBuilder;
import org.jasonjson.core.JsonElement;
import org.jasonjson.core.JsonNull;
import org.jasonjson.core.JsonObject;
import org.jasonjson.core.JsonDeserializationContext;
import org.jasonjson.core.JsonDeserializer;
import org.jasonjson.core.JsonSerializationContext;
import org.jasonjson.core.JsonSerializer;
import org.jasonjson.core.common.TestTypes.BagOfPrimitives;
import org.jasonjson.core.common.TestTypes.ClassWithObjects;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Functional tests for the different cases for serializing (or ignoring) null fields and object.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class NullObjectAndFieldTest extends TestCase {
  private JasonBuilder gsonBuilder;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gsonBuilder = new JasonBuilder().serializeNulls();
  }

  public void testTopLevelNullObjectSerialization() {
    Jason gson = gsonBuilder.create();
    String actual = gson.toJson(null);
    assertEquals("null", actual);

    actual = gson.toJson(null, String.class);
    assertEquals("null", actual);
  }

  public void testTopLevelNullObjectDeserialization() throws Exception {
    Jason gson = gsonBuilder.create();
    String actual = gson.fromJson("null", String.class);
    assertNull(actual);
  }

  public void testExplicitSerializationOfNulls() {
    Jason gson = gsonBuilder.create();
    ClassWithObjects target = new ClassWithObjects(null);
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertEquals(expected, actual);
  }

  public void testExplicitDeserializationOfNulls() throws Exception {
    Jason gson = gsonBuilder.create();
    ClassWithObjects target = gson.fromJson("{\"bag\":null}", ClassWithObjects.class);
    assertNull(target.bag);
  }
  
  public void testExplicitSerializationOfNullArrayMembers() {
    Jason gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"array\":null"));
  }
  
  /** 
   * Added to verify http://code.google.com/p/google-gson/issues/detail?id=68
   */
  public void testNullWrappedPrimitiveMemberSerialization() {
    Jason gson = gsonBuilder.serializeNulls().create();
    ClassWithNullWrappedPrimitive target = new ClassWithNullWrappedPrimitive();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"value\":null"));
  }
  
  /** 
   * Added to verify http://code.google.com/p/google-gson/issues/detail?id=68
   */
  public void testNullWrappedPrimitiveMemberDeserialization() {
    Jason gson = gsonBuilder.create();
    String json = "{'value':null}";
    ClassWithNullWrappedPrimitive target = gson.fromJson(json, ClassWithNullWrappedPrimitive.class);
    assertNull(target.value);
  }
  
  public void testExplicitSerializationOfNullCollectionMembers() {
    Jason gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"col\":null"));
  }
  
  public void testExplicitSerializationOfNullStringMembers() {
    Jason gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"str\":null"));
  }

  public void testCustomSerializationOfNulls() {
    gsonBuilder.registerTypeAdapter(ClassWithObjects.class, new ClassWithObjectsSerializer());
    Jason gson = gsonBuilder.create();
    ClassWithObjects target = new ClassWithObjects(new BagOfPrimitives());
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertEquals(expected, actual);
  }
  
  public void testPrintPrintingObjectWithNulls() throws Exception {
    gsonBuilder = new JasonBuilder();
    Jason gson = gsonBuilder.create();
    String result = gson.toJson(new ClassWithMembers());
    assertEquals("{}", result);

    gson = gsonBuilder.serializeNulls().create();
    result = gson.toJson(new ClassWithMembers());
    assertTrue(result.contains("\"str\":null"));
  }
  
  public void testPrintPrintingArraysWithNulls() throws Exception {
    gsonBuilder = new JasonBuilder();
    Jason gson = gsonBuilder.create();
    String result = gson.toJson(new String[] { "1", null, "3" });
    assertEquals("[\"1\",null,\"3\"]", result);

    gson = gsonBuilder.serializeNulls().create();
    result = gson.toJson(new String[] { "1", null, "3" });
    assertEquals("[\"1\",null,\"3\"]", result);
  }

  // test for issue 389
  public void testAbsentJsonElementsAreSetToNull() {
    Jason gson = new Jason();
    ClassWithInitializedMembers target =
        gson.fromJson("{array:[1,2,3]}", ClassWithInitializedMembers.class);
    assertTrue(target.array.length == 3 && target.array[1] == 2);
    assertEquals(ClassWithInitializedMembers.MY_STRING_DEFAULT, target.str1);
    assertNull(target.str2);
    assertEquals(ClassWithInitializedMembers.MY_INT_DEFAULT, target.int1);
    assertEquals(0, target.int2); // test the default value of a primitive int field per JVM spec
    assertEquals(ClassWithInitializedMembers.MY_BOOLEAN_DEFAULT, target.bool1);
    assertFalse(target.bool2); // test the default value of a primitive boolean field per JVM spec
  }

  public static class ClassWithInitializedMembers  {
    // Using a mix of no-args constructor and field initializers
    // Also, some fields are intialized and some are not (so initialized per JVM spec)
    public static final String MY_STRING_DEFAULT = "string";
    private static final int MY_INT_DEFAULT = 2;
    private static final boolean MY_BOOLEAN_DEFAULT = true;
    int[] array;
    String str1, str2;
    int int1 = MY_INT_DEFAULT;
    int int2;
    boolean bool1 = MY_BOOLEAN_DEFAULT;
    boolean bool2;
    public ClassWithInitializedMembers() {
      str1 = MY_STRING_DEFAULT;
    }
  }

  private static class ClassWithNullWrappedPrimitive {
    private Long value;
  }

  @SuppressWarnings("unused")
  private static class ClassWithMembers {
    String str;
    int[] array;
    Collection<String> col;
  }
  
  private static class ClassWithObjectsSerializer implements JsonSerializer<ClassWithObjects> {
    public JsonElement serialize(ClassWithObjects src, Type typeOfSrc,
        JsonSerializationContext context) {
      JsonObject obj = new JsonObject();
      obj.add("bag", JsonNull.INSTANCE);
      return obj;
    }
  }

  public void testExplicitNullSetsFieldToNullDuringDeserialization() {
    Jason gson = new Jason();
    String json = "{value:null}";
    ObjectWithField obj = gson.fromJson(json, ObjectWithField.class);
    assertNull(obj.value);    
  }

  public void testCustomTypeAdapterPassesNullSerialization() {
    Jason gson = new JasonBuilder()
        .registerTypeAdapter(ObjectWithField.class, new JsonSerializer<ObjectWithField>() {
          public JsonElement serialize(ObjectWithField src, Type typeOfSrc,
              JsonSerializationContext context) {
            return context.serialize(null);
          }
        }).create();
    ObjectWithField target = new ObjectWithField();
    target.value = "value1";
    String json = gson.toJson(target);
    assertFalse(json.contains("value1"));
  }

  public void testCustomTypeAdapterPassesNullDesrialization() {
    Jason gson = new JasonBuilder()
        .registerTypeAdapter(ObjectWithField.class, new JsonDeserializer<ObjectWithField>() {
          public ObjectWithField deserialize(JsonElement json, Type type,
              JsonDeserializationContext context) {
            return context.deserialize(null, type);
          }
        }).create();
    String json = "{value:'value1'}";
    ObjectWithField target = gson.fromJson(json, ObjectWithField.class);
    assertNull(target);
  }

  private static class ObjectWithField {
    String value = "";
  }
}
