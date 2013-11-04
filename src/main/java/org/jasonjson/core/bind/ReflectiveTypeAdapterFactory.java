/*
 * Copyright (C) 2011 Google Inc.
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

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;
import org.jasonjson.core.AccessStrategy;
import org.jasonjson.core.attribute.Attribute;

/**
 * Type adapter that reflects over the fields and methods of a class.
 */
public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {

    private final ConstructorConstructor constructorConstructor;

    private final FieldNamingStrategy fieldNamingPolicy;

    private final Excluder excluder;

    private final AccessStrategy accessStrategy;

    public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor,
                                        FieldNamingStrategy fieldNamingPolicy, Excluder excluder,
                                        AccessStrategy accessStrategy) {
        this.constructorConstructor = constructorConstructor;
        this.fieldNamingPolicy = fieldNamingPolicy;
        this.excluder = excluder;
        this.accessStrategy = accessStrategy;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
        Class<? super T> raw = type.getRawType();

        if (!Object.class.isAssignableFrom(raw)) {
            return null; // it's a primitive!
        }

        ObjectConstructor<T> constructor = constructorConstructor.get(type);
        return new DefaultAdapter<T>(constructor, getBoundFields(gson, type, raw, accessStrategy), type);
    }

    private boolean isAttributeExcluded(Attribute attribute, boolean serialization) {
        boolean excludeClass = excluder.excludeClass(attribute.getDeclaredClass(), serialization);
        boolean excludeField = excluder.excludeField(attribute, serialization);
        return excludeClass || excludeField;
    }

    private String getAttributeName(Attribute attribute) {
        SerializedName serializedName = attribute.getAnnotation(SerializedName.class);
        return serializedName == null ? fieldNamingPolicy.translateName(attribute) : serializedName.value();
    }

    private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw, AccessStrategy accessStrategy) {
        Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
        if (raw.isInterface()) {
            return result;
        }

        Type declaredType = type.getType();
        while (raw != Object.class) {
            for (Attribute field : AttributesExtractor.extractAttributes(raw, accessStrategy.chooseStrategy(raw))) {
                boolean serialize = !isAttributeExcluded(field, true);
                boolean deserialize = !isAttributeExcluded(field, false);
                if (!serialize && !deserialize) {
                    continue;
                }

                Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getDeclaredType());
                BoundField boundField = BoundFieldFactory.createInstance(context, field, getAttributeName(field), TypeToken.get(fieldType), serialize, deserialize);
                BoundField previous = result.put(boundField.name, boundField);
                if (previous != null) {
                    throw new IllegalArgumentException(declaredType + " declares multiple JSON fields named " + previous.name);
                }
            }
            type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return result;
    }
}
