/*
 * Copyright (C) 2012 Google Inc.
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
package org.jasonjson.core.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jasonjson.core.Jason;
import org.jasonjson.core.JasonBuilder;
import org.jasonjson.core.filter.RuntimeFilters;
import org.jasonjson.core.TypeAdapter;
import org.jasonjson.core.TypeAdapterFactory;
import org.jasonjson.core.common.TestTypes.BagOfPrimitives;
import org.jasonjson.core.reflect.TypeToken;
import org.jasonjson.core.stream.JsonReader;
import org.jasonjson.core.stream.JsonWriter;

/**
 * Functional tests for {@link Gson#getDelegateAdapter(TypeAdapterFactory, TypeToken)} method.
 *
 * @author Inderjeet Singh
 */
public class DelegateTypeAdapterTest extends TestCase {

  private StatsTypeAdapterFactory stats;
  private Jason gson;
  protected void setUp() throws Exception {
    super.setUp();
    stats = new StatsTypeAdapterFactory();
    gson = new JasonBuilder()
      .registerTypeAdapterFactory(stats)
      .create();
  }

  public void testDelegateInvoked() {
    List<BagOfPrimitives> bags = new ArrayList<BagOfPrimitives>();
    for (int i = 0; i < 10; ++i) {
      bags.add(new BagOfPrimitives(i, i, i % 2 == 0, String.valueOf(i)));
    }
    String json = gson.toJson(bags);
    bags = gson.fromJson(json, new TypeToken<List<BagOfPrimitives>>(){}.getType());
    // 11: 1 list object, and 10 entries. stats invoked on all 5 fields
    assertEquals(51, stats.numReads);
    assertEquals(51, stats.numWrites);
  }

  public void testDelegateInvokedOnStrings() {
    String[] bags = {"1", "2", "3", "4"};
    String json = gson.toJson(bags);
    bags = gson.fromJson(json, String[].class);
    // 1 array object with 4 elements.
    assertEquals(5, stats.numReads);
    assertEquals(5, stats.numWrites);
  }

  private static class StatsTypeAdapterFactory implements TypeAdapterFactory {
    public int numReads = 0;
    public int numWrites = 0;

    public <T> TypeAdapter<T> create(Jason gson, TypeToken<T> type) {
      final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
      return new TypeAdapter<T>() {
        @Override
        public void write(JsonWriter out, T value, RuntimeFilters runtimeFilters) throws IOException {
          ++numWrites;
          delegate.write(out, value, runtimeFilters);
        }

        @Override
        public T read(JsonReader in, RuntimeFilters runtimeFilters) throws IOException {
          ++numReads;
          return delegate.read(in, runtimeFilters);
        }
      };
    }
  }
}
