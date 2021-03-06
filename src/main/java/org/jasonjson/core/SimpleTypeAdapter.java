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
package org.jasonjson.core;

import org.jasonjson.core.stream.JsonReader;
import org.jasonjson.core.filter.RuntimeFilters;
import org.jasonjson.core.stream.JsonWriter;
import java.io.IOException;

/**
 *
 * @author Adam Dubiel
 */
public abstract class SimpleTypeAdapter<T> extends TypeAdapter<T> {

    public abstract void write(JsonWriter out, T value) throws IOException;

    @Override
    public void write(JsonWriter out, T value, RuntimeFilters runtimeFilters) throws IOException {
        write(out, value);
    }

    public abstract T read(JsonReader in) throws IOException;

    @Override
    public T read(JsonReader in, RuntimeFilters runtimeFilters) throws IOException {
        return read(in);
    }
}
