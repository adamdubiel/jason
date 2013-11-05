/*
 * Copyright (C) 2009 Google Inc.
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
import org.jasonjson.core.JsonArray;
import org.jasonjson.core.JsonElement;
import org.jasonjson.core.JsonObject;
import org.jasonjson.core.JsonSerializationContext;
import org.jasonjson.core.JsonSerializer;
import org.jasonjson.core.common.TestTypes.Base;
import org.jasonjson.core.common.TestTypes.BaseSerializer;
import org.jasonjson.core.common.TestTypes.ClassWithBaseArrayField;
import org.jasonjson.core.common.TestTypes.ClassWithBaseField;
import org.jasonjson.core.common.TestTypes.Sub;
import org.jasonjson.core.common.TestTypes.SubSerializer;

import junit.framework.TestCase;

import java.lang.reflect.Type;

/**
 * Functional Test exercising custom serialization only.  When test applies to both
 * serialization and deserialization then add it to CustomTypeAdapterTest.
 *
 * @author Inderjeet Singh
 */
public class CustomSerializerTest extends TestCase {

   public void testBaseClassSerializerInvokedForBaseClassFields() {
     Jason gson = new JasonBuilder()
         .registerTypeAdapter(Base.class, new BaseSerializer())
         .registerTypeAdapter(Sub.class, new SubSerializer())
         .create();
     ClassWithBaseField target = new ClassWithBaseField(new Base());
     JsonObject json = (JsonObject) gson.toJsonTree(target);
     JsonObject base = json.get("base").getAsJsonObject();
     assertEquals(BaseSerializer.NAME, base.get(Base.SERIALIZER_KEY).getAsString());
   }

   public void testSubClassSerializerInvokedForBaseClassFieldsHoldingSubClassInstances() {
     Jason gson = new JasonBuilder()
         .registerTypeAdapter(Base.class, new BaseSerializer())
         .registerTypeAdapter(Sub.class, new SubSerializer())
         .create();
     ClassWithBaseField target = new ClassWithBaseField(new Sub());
     JsonObject json = (JsonObject) gson.toJsonTree(target);
     JsonObject base = json.get("base").getAsJsonObject();
     assertEquals(SubSerializer.NAME, base.get(Base.SERIALIZER_KEY).getAsString());
   }

   public void testSubClassSerializerInvokedForBaseClassFieldsHoldingArrayOfSubClassInstances() {
     Jason gson = new JasonBuilder()
         .registerTypeAdapter(Base.class, new BaseSerializer())
         .registerTypeAdapter(Sub.class, new SubSerializer())
         .create();
     ClassWithBaseArrayField target = new ClassWithBaseArrayField(new Base[] {new Sub(), new Sub()});
     JsonObject json = (JsonObject) gson.toJsonTree(target);
     JsonArray array = json.get("base").getAsJsonArray();
     for (JsonElement element : array) {
       JsonElement serializerKey = element.getAsJsonObject().get(Base.SERIALIZER_KEY);
      assertEquals(SubSerializer.NAME, serializerKey.getAsString());
     }
   }

   public void testBaseClassSerializerInvokedForBaseClassFieldsHoldingSubClassInstances() {
     Jason gson = new JasonBuilder()
         .registerTypeAdapter(Base.class, new BaseSerializer())
         .create();
     ClassWithBaseField target = new ClassWithBaseField(new Sub());
     JsonObject json = (JsonObject) gson.toJsonTree(target);
     JsonObject base = json.get("base").getAsJsonObject();
     assertEquals(BaseSerializer.NAME, base.get(Base.SERIALIZER_KEY).getAsString());
   }

   public void testSerializerReturnsNull() {
     Jason gson = new JasonBuilder()
       .registerTypeAdapter(Base.class, new JsonSerializer<Base>() {
         public JsonElement serialize(Base src, Type typeOfSrc, JsonSerializationContext context) {
           return null;
         }
       })
       .create();
       JsonElement json = gson.toJsonTree(new Base());
       assertTrue(json.isJsonNull());
   }
}
