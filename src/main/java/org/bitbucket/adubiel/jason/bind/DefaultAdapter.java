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

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.bitbucket.adubiel.jason.transform.RuntimeTransformer;

/**
 *
 * @author Adam Dubiel
 */
class DefaultAdapter<T> extends TypeAdapter<T> {

    private final ObjectConstructor<T> constructor;

    private final Map<String, BoundField> boundFields;

    private final TypeToken<T> type;

    DefaultAdapter(ObjectConstructor<T> constructor, Map<String, BoundField> boundFields, TypeToken<T> type) {
        this.constructor = constructor;
        this.boundFields = boundFields;
        this.type = type;
    }

    @Override
    public T read(JsonReader in, RuntimeTransformer runtimeTransformer) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        T instance = constructor.construct();

        try {
            in.beginObject();
            while (in.hasNext()) {
                String name = runtimeTransformer.transformName(type, in.nextName());
                BoundField field = boundFields.get(name);
                if (field == null || !field.deserialized || runtimeTransformer.skipField(type, name)) {
                    in.skipValue();
                } else {
                    field.read(in, instance, runtimeTransformer);
                }
            }
        } catch (IllegalStateException e) {
            throw new JsonSyntaxException(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        }

        in.endObject();
        return instance;
    }

    @Override
    public void write(JsonWriter out, T value, RuntimeTransformer transformer) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.beginObject();
        try {
            for (BoundField boundField : boundFields.values()) {
                if (boundField.serialized && !transformer.skipField(type, boundField.name)) {
                    out.name(transformer.transformName(type, boundField.name));
                    boundField.write(out, value, transformer);
                }
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        }

        out.endObject();
    }
}
