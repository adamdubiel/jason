/*
 * Copyright 2013 Adam Dubiel.
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
package org.jasonjson.core.bind;

import org.jasonjson.core.stream.JsonReader;
import org.jasonjson.core.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.jasonjson.core.filter.RuntimeFilters;

/**
 *
 * @author Adam Dubiel
 */
abstract class BoundField {

    final String name;

    final boolean serialized;

    final boolean deserialized;

    final boolean field;

    protected BoundField(String name, boolean serialized, boolean deserialized, boolean field) {
        this.name = name;
        this.serialized = serialized;
        this.deserialized = deserialized;
        this.field = field;
    }

    abstract void write(JsonWriter writer, Object value, RuntimeFilters runtimeFilters) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    abstract void read(JsonReader reader, Object value, RuntimeFilters runtimeFilters) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

}
