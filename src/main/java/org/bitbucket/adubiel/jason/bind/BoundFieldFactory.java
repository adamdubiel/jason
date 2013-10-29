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
package org.bitbucket.adubiel.jason.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.bitbucket.adubiel.jason.attribute.Attribute;
import org.bitbucket.adubiel.jason.transform.RuntimeTransformer;

/**
 *
 * @author Adam Dubiel
 */
final class BoundFieldFactory {

    private BoundFieldFactory() {
    }

    static BoundField createInstance(final Gson context, final Attribute attribute, final String name,
                                     final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
        // special casing primitives here saves ~5% on Android...
        final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());

        return new BoundField(name, serialize, deserialize) {
            final TypeAdapter<?> typeAdapter = context.getAdapter(fieldType);

            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            void write(JsonWriter writer, Object value, RuntimeTransformer runtimeTransformer)
                    throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                Object fieldValue = attribute.get(value);
                TypeAdapter adapterWrapper = new TypeAdapterRuntimeTypeWrapper(context, this.typeAdapter, fieldType.getType());
                adapterWrapper.write(writer, fieldValue, runtimeTransformer);
            }

            @Override
            void read(JsonReader reader, Object value, RuntimeTransformer runtimeTransformer)
                    throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                Object fieldValue = typeAdapter.read(reader, runtimeTransformer);
                if (fieldValue != null || !isPrimitive) {
                    attribute.set(value, fieldValue);
                }
            }
        };

    }

}
