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
import org.jasonjson.core.common.TestTypes.BagOfPrimitives;

import junit.framework.TestCase;

/**
 * Tests for security-related aspects of Jason
 * 
 * @author Inderjeet Singh
 */
public class SecurityTest extends TestCase {
  /**
   * Keep this in sync with Jason.JSON_NON_EXECUTABLE_PREFIX
   */
  private static final String JSON_NON_EXECUTABLE_PREFIX = ")]}'\n";

  private JasonBuilder gsonBuilder;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gsonBuilder = new JasonBuilder();
  }

  public void testNonExecutableJsonSerialization() {
    Jason gson = gsonBuilder.generateNonExecutableJson().create();
    String json = gson.toJson(new BagOfPrimitives());
    assertTrue(json.startsWith(JSON_NON_EXECUTABLE_PREFIX));
  }
  
  public void testNonExecutableJsonDeserialization() {
    String json = JSON_NON_EXECUTABLE_PREFIX + "{longValue:1}";
    Jason gson = gsonBuilder.create();
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(1, target.longValue);
  }
  
  public void testJsonWithNonExectuableTokenSerialization() {
    Jason gson = gsonBuilder.generateNonExecutableJson().create();
    String json = gson.toJson(JSON_NON_EXECUTABLE_PREFIX);
    assertTrue(json.contains(")]}'\n"));
  }
  
  /**
   *  Jason should be able to deserialize a stream with non-exectuable token even if it is created
  without {@link GsonBuilder#generateNonExecutableJson()}.
   */
  public void testJsonWithNonExectuableTokenWithRegularGsonDeserialization() {
    Jason gson = gsonBuilder.create();
    String json = JSON_NON_EXECUTABLE_PREFIX + "{stringValue:')]}\\u0027\\n'}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(")]}'\n", target.stringValue);
  }  
  
  /**
   *  Jason should be able to deserialize a stream with non-exectuable token if it is created
  with {@link GsonBuilder#generateNonExecutableJson()}.
   */
  public void testJsonWithNonExectuableTokenWithConfiguredGsonDeserialization() {
    // Jason should be able to deserialize a stream with non-exectuable token even if it is created 
    Jason gson = gsonBuilder.generateNonExecutableJson().create();
    String json = JSON_NON_EXECUTABLE_PREFIX + "{intValue:2,stringValue:')]}\\u0027\\n'}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(")]}'\n", target.stringValue);
    assertEquals(2, target.intValue);
  }  
}
