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

package org.jasonjson.core;

import org.jasonjson.core.filter.RuntimeFilters;
import org.jasonjson.core.filter.EmptyRuntimeFilters;
import org.jasonjson.core.internal.ConstructorConstructor;
import org.jasonjson.core.internal.Excluder;
import org.jasonjson.core.internal.Primitives;
import org.jasonjson.core.internal.Streams;
import org.jasonjson.core.internal.bind.ArrayTypeAdapter;
import org.jasonjson.core.internal.bind.CollectionTypeAdapterFactory;
import org.jasonjson.core.internal.bind.DateTypeAdapter;
import org.jasonjson.core.internal.bind.JsonTreeReader;
import org.jasonjson.core.internal.bind.JsonTreeWriter;
import org.jasonjson.core.internal.bind.MapTypeAdapterFactory;
import org.jasonjson.core.internal.bind.ObjectTypeAdapter;
import org.jasonjson.core.internal.bind.SqlDateTypeAdapter;
import org.jasonjson.core.internal.bind.TimeTypeAdapter;
import org.jasonjson.core.internal.bind.TypeAdapters;
import org.jasonjson.core.reflect.TypeToken;
import org.jasonjson.core.stream.JsonReader;
import org.jasonjson.core.stream.JsonToken;
import org.jasonjson.core.stream.JsonWriter;
import org.jasonjson.core.stream.MalformedJsonException;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jasonjson.core.AccessStrategy;
import org.jasonjson.core.AnnotationDefinedAccessStrategy;
import org.jasonjson.core.bind.ReflectiveTypeAdapterFactory;

/**
 * This is the main class for using Jason. Jason is typically used by first constructing a
 Jason instance and then invoking {@link #toJson(Object)} or {@link #fromJson(String, Class)}
 * methods on it.
 *
 * <p>You can create a Jason instance by invoking {@literal new Jason()} if the default configuration
 * is all you need. You can also use {@link GsonBuilder} to build a Jason instance with various
 configuration options such as versioning support, pretty printing, custom
 {@link JsonSerializer}s, {@link JsonDeserializer}s, and {@link InstanceCreator}s.</p>
 *
 * <p>Here is an example of how Jason is used for a simple Class:

 <pre>
 Jason gson = new Jason(); // Or use new JasonBuilder().create();
 MyType target = new MyType();
 String json = gson.toJson(target); // serializes target to Json
 MyType target2 = gson.fromJson(json, MyType.class); // deserializes json into target2
 </pre></p>
 *
 * <p>If the object that your are serializing/deserializing is a {@literal ParameterizedType}
 * (i.e. contains at least one type parameter and may be an array) then you must use the
 * {@link #toJson(Object, Type)} or {@link #fromJson(String, Type)} method.  Here is an
 * example for serializing and deserialing a {@literal ParameterizedType}:
 *
 * <pre>
 * Type listType = new TypeToken&lt;List&lt;String&gt;&gt;() {}.getType();
 * List&lt;String&gt; target = new LinkedList&lt;String&gt;();
 target.add("blah");

 Jason gson = new Jason();
 String json = gson.toJson(target, listType);
 List&lt;String&gt; target2 = gson.fromJson(json, listType);
 * </pre></p>
 *
 * <p>See the <a href="https://sites.google.com/site/gson/gson-user-guide">Jason User Guide</a>
 * for a more complete set of examples.</p>
 *
 * @see com.google.gson.reflect.TypeToken
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @author Jesse Wilson
 */
public final class Jason {
  static final boolean DEFAULT_JSON_NON_EXECUTABLE = false;

  private static final String JSON_NON_EXECUTABLE_PREFIX = ")]}'\n";

  private static final RuntimeFilters EMPTY_RUNTIME_TRANSFORMER = new EmptyRuntimeFilters();

  /**
   * This thread local guards against reentrant calls to getAdapter(). In
   * certain object graphs, creating an adapter for a type may recursively
   * require an adapter for the same type! Without intervention, the recursive
   * lookup would stack overflow. We cheat by returning a proxy type adapter.
   * The proxy is wired up once the initial adapter has been created.
   */
  private final ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>> calls
      = new ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>>();

  private final Map<TypeToken<?>, TypeAdapter<?>> typeTokenCache
      = Collections.synchronizedMap(new HashMap<TypeToken<?>, TypeAdapter<?>>());

  private final List<TypeAdapterFactory> factories;
  private final ConstructorConstructor constructorConstructor;

  private final boolean serializeNulls;
  private final boolean htmlSafe;
  private final boolean generateNonExecutableJson;
  private final boolean prettyPrinting;

  final JsonDeserializationContext deserializationContext = new JsonDeserializationContext() {
    @SuppressWarnings("unchecked")
	public <T> T deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
      return (T) fromJson(json, typeOfT);
    }
  };

  final JsonSerializationContext serializationContext = new JsonSerializationContext() {
    public JsonElement serialize(Object src) {
      return toJsonTree(src);
    }
    public JsonElement serialize(Object src, Type typeOfSrc) {
      return toJsonTree(src, typeOfSrc);
    }
  };

  /**
   * Constructs a Gson object with default configuration. The default configuration has the
   * following settings:
   * <ul>
   *   <li>The JSON generated by <code>toJson</code> methods is in compact representation. This
   *   means that all the unneeded white-space is removed. You can change this behavior with
   *   {@link GsonBuilder#setPrettyPrinting()}. </li>
   *   <li>The generated JSON omits all the fields that are null. Note that nulls in arrays are
   *   kept as is since an array is an ordered list. Moreover, if a field is not null, but its
   *   generated JSON is empty, the field is kept. You can configure Gson to serialize null values
   *   by setting {@link GsonBuilder#serializeNulls()}.</li>
   *   <li>Gson provides default serialization and deserialization for Enums, {@link Map},
   *   {@link java.net.URL}, {@link java.net.URI}, {@link java.util.Locale}, {@link java.util.Date},
   *   {@link java.math.BigDecimal}, and {@link java.math.BigInteger} classes. If you would prefer
   *   to change the default representation, you can do so by registering a type adapter through
   *   {@link GsonBuilder#registerTypeAdapter(Type, Object)}. </li>
   *   <li>The default Date format is same as {@link java.text.DateFormat#DEFAULT}. This format
   *   ignores the millisecond portion of the date during serialization. You can change
   *   this by invoking {@link GsonBuilder#setDateFormat(int)} or
   *   {@link GsonBuilder#setDateFormat(String)}. </li>
   *   <li>By default, Gson ignores the {@link com.google.gson.annotations.Expose} annotation.
   *   You can enable Gson to serialize/deserialize only those fields marked with this annotation
   *   through {@link GsonBuilder#excludeFieldsWithoutExposeAnnotation()}. </li>
   *   <li>By default, Gson ignores the {@link com.google.gson.annotations.Since} annotation. You
   *   can enable Gson to use this annotation through {@link GsonBuilder#setVersion(double)}.</li>
   *   <li>The default field naming policy for the output Json is same as in Java. So, a Java class
   *   field <code>versionNumber</code> will be output as <code>&quot;versionNumber@quot;</code> in
   *   Json. The same rules are applied for mapping incoming Json to the Java classes. You can
   *   change this policy through {@link GsonBuilder#setFieldNamingPolicy(FieldNamingPolicy)}.</li>
   *   <li>By default, Gson excludes <code>transient</code> or <code>static</code> fields from
   *   consideration for serialization and deserialization. You can change this behavior through
   *   {@link GsonBuilder#excludeFieldsWithModifiers(int...)}.</li>
   * </ul>
   */
  public Jason() {
    this(Excluder.DEFAULT, FieldNamingPolicy.IDENTITY, new AnnotationDefinedAccessStrategy(),
        Collections.<Type, InstanceCreator<?>>emptyMap(), false, false, DEFAULT_JSON_NON_EXECUTABLE,
        true, false, false, LongSerializationPolicy.DEFAULT,
        Collections.<TypeAdapterFactory>emptyList());
  }

  Jason(final Excluder excluder, final FieldNamingStrategy fieldNamingPolicy,
      final AccessStrategy accessStrategy,
      final Map<Type, InstanceCreator<?>> instanceCreators, boolean serializeNulls,
      boolean complexMapKeySerialization, boolean generateNonExecutableGson, boolean htmlSafe,
      boolean prettyPrinting, boolean serializeSpecialFloatingPointValues,
      LongSerializationPolicy longSerializationPolicy,
      List<TypeAdapterFactory> typeAdapterFactories) {
    this.constructorConstructor = new ConstructorConstructor(instanceCreators);
    this.serializeNulls = serializeNulls;
    this.generateNonExecutableJson = generateNonExecutableGson;
    this.htmlSafe = htmlSafe;
    this.prettyPrinting = prettyPrinting;

    List<TypeAdapterFactory> factories = new ArrayList<TypeAdapterFactory>();

    // built-in type adapters that cannot be overridden
    factories.add(TypeAdapters.JSON_ELEMENT_FACTORY);
    factories.add(ObjectTypeAdapter.FACTORY);

    // the excluder must precede all adapters that handle user-defined types
    factories.add(excluder);

    // user's type adapters
    factories.addAll(typeAdapterFactories);

    // type adapters for basic platform types
    factories.add(TypeAdapters.STRING_FACTORY);
    factories.add(TypeAdapters.INTEGER_FACTORY);
    factories.add(TypeAdapters.BOOLEAN_FACTORY);
    factories.add(TypeAdapters.BYTE_FACTORY);
    factories.add(TypeAdapters.SHORT_FACTORY);
    factories.add(TypeAdapters.newFactory(long.class, Long.class,
            longAdapter(longSerializationPolicy)));
    factories.add(TypeAdapters.newFactory(double.class, Double.class,
            doubleAdapter(serializeSpecialFloatingPointValues)));
    factories.add(TypeAdapters.newFactory(float.class, Float.class,
            floatAdapter(serializeSpecialFloatingPointValues)));
    factories.add(TypeAdapters.NUMBER_FACTORY);
    factories.add(TypeAdapters.CHARACTER_FACTORY);
    factories.add(TypeAdapters.STRING_BUILDER_FACTORY);
    factories.add(TypeAdapters.STRING_BUFFER_FACTORY);
    factories.add(TypeAdapters.newFactory(BigDecimal.class, TypeAdapters.BIG_DECIMAL));
    factories.add(TypeAdapters.newFactory(BigInteger.class, TypeAdapters.BIG_INTEGER));
    factories.add(TypeAdapters.URL_FACTORY);
    factories.add(TypeAdapters.URI_FACTORY);
    factories.add(TypeAdapters.UUID_FACTORY);
    factories.add(TypeAdapters.LOCALE_FACTORY);
    factories.add(TypeAdapters.INET_ADDRESS_FACTORY);
    factories.add(TypeAdapters.BIT_SET_FACTORY);
    factories.add(DateTypeAdapter.FACTORY);
    factories.add(TypeAdapters.CALENDAR_FACTORY);
    factories.add(TimeTypeAdapter.FACTORY);
    factories.add(SqlDateTypeAdapter.FACTORY);
    factories.add(TypeAdapters.TIMESTAMP_FACTORY);
    factories.add(ArrayTypeAdapter.FACTORY);
    factories.add(TypeAdapters.ENUM_FACTORY);
    factories.add(TypeAdapters.CLASS_FACTORY);

    // type adapters for composite and user-defined types
    factories.add(new CollectionTypeAdapterFactory(constructorConstructor));
    factories.add(new MapTypeAdapterFactory(constructorConstructor, complexMapKeySerialization));
    factories.add(new ReflectiveTypeAdapterFactory(
        constructorConstructor, fieldNamingPolicy, excluder, accessStrategy));

    this.factories = Collections.unmodifiableList(factories);
  }

  private TypeAdapter<Number> doubleAdapter(boolean serializeSpecialFloatingPointValues) {
    if (serializeSpecialFloatingPointValues) {
      return TypeAdapters.DOUBLE;
    }
    return new SimpleTypeAdapter<Number>() {
      @Override public Double read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }
        return in.nextDouble();
      }
      @Override public void write(JsonWriter out, Number value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }
        double doubleValue = value.doubleValue();
        checkValidFloatingPoint(doubleValue);
        out.value(value);
      }
    };
  }

  private TypeAdapter<Number> floatAdapter(boolean serializeSpecialFloatingPointValues) {
    if (serializeSpecialFloatingPointValues) {
      return TypeAdapters.FLOAT;
    }
    return new SimpleTypeAdapter<Number>() {
      @Override public Float read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }
        return (float) in.nextDouble();
      }
      @Override public void write(JsonWriter out, Number value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }
        float floatValue = value.floatValue();
        checkValidFloatingPoint(floatValue);
        out.value(value);
      }
    };
  }

  private void checkValidFloatingPoint(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      throw new IllegalArgumentException(value
          + " is not a valid double value as per JSON specification. To override this"
          + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.");
    }
  }

  private TypeAdapter<Number> longAdapter(LongSerializationPolicy longSerializationPolicy) {
    if (longSerializationPolicy == LongSerializationPolicy.DEFAULT) {
      return TypeAdapters.LONG;
    }
    return new SimpleTypeAdapter<Number>() {
      @Override public Number read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }
        return in.nextLong();
      }
      @Override public void write(JsonWriter out, Number value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }
        out.value(value.toString());
      }
    };
  }

  /**
   * Returns the type adapter for {@literal } type.
   *
   * @throws IllegalArgumentException if this GSON cannot serialize and
   *     deserialize {@literal type}.
   */
  @SuppressWarnings("unchecked")
  public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
    TypeAdapter<?> cached = typeTokenCache.get(type);
    if (cached != null) {
      return (TypeAdapter<T>) cached;
    }

    Map<TypeToken<?>, FutureTypeAdapter<?>> threadCalls = calls.get();
    boolean requiresThreadLocalCleanup = false;
    if (threadCalls == null) {
      threadCalls = new HashMap<TypeToken<?>, FutureTypeAdapter<?>>();
      calls.set(threadCalls);
      requiresThreadLocalCleanup = true;
    }

    // the key and value type parameters always agree
    FutureTypeAdapter<T> ongoingCall = (FutureTypeAdapter<T>) threadCalls.get(type);
    if (ongoingCall != null) {
      return ongoingCall;
    }

    try {
      FutureTypeAdapter<T> call = new FutureTypeAdapter<T>();
      threadCalls.put(type, call);

      for (TypeAdapterFactory factory : factories) {
        TypeAdapter<T> candidate = factory.create(this, type);
        if (candidate != null) {
          call.setDelegate(candidate);
          typeTokenCache.put(type, candidate);
          return candidate;
        }
      }
      throw new IllegalArgumentException("GSON cannot handle " + type);
    } finally {
      threadCalls.remove(type);

      if (requiresThreadLocalCleanup) {
        calls.remove();
      }
    }
  }

  /**
   * This method is used to get an alternate type adapter for the specified type. This is used
   * to access a type adapter that is overridden by a {@link TypeAdapterFactory} that you
 may have registered. This features is typically used when you want to register a type
 adapter that does a little bit of work but then delegates further processing to the Jason
 default type adapter. Here is an example:
 <p>Let's say we want to write a type adapter that counts the number of objects being read
   *  from or written to JSON. We can achieve this by writing a type adapter factory that uses
   *  the <code>getDelegateAdapter</code> method:
   *  <pre> {@literal class StatsTypeAdapterFactory implements TypeAdapterFactory {
    public int numReads = 0;
    public int numWrites = 0;
    public &lt;T&gt; TypeAdapter&lt;T&gt; create(Jason gson, TypeToken&lt;T&gt; type) {
      final TypeAdapter&lt;T&gt; delegate = gson.getDelegateAdapter(this, type);
      return new TypeAdapter&lt;T&gt;() {
        public void write(JsonWriter out, T value) throws IOException {
          ++numWrites;
          delegate.write(out, value);
        }
        public T read(JsonReader in) throws IOException {
          ++numReads;
          return delegate.read(in);
        }
      };
    }
  }
  } </pre>
   *  This factory can now be used like this:
   *  <pre> {@literal StatsTypeAdapterFactory stats = new StatsTypeAdapterFactory();
  Jason gson = new JasonBuilder().registerTypeAdapterFactory(stats).create();
  // Call gson.toJson() and fromJson methods on objects
  System.out.println("Num JSON reads" + stats.numReads);
  System.out.println("Num JSON writes" + stats.numWrites);
  }</pre>
   *  Note that since you can not override type adapter factories for String and Java primitive
   *  types, our stats factory will not count the number of String or primitives that will be
   *  read or written.
   * @param skipPast The type adapter factory that needs to be skipped while searching for
   *   a matching type adapter. In most cases, you should just pass <i>this</i> (the type adapter
   *   factory from where {@link #getDelegateAdapter} method is being invoked).
   * @param type Type for which the delegate adapter is being searched for.
   *
   * @since 2.2
   */
  public <T> TypeAdapter<T> getDelegateAdapter(TypeAdapterFactory skipPast, TypeToken<T> type) {
    boolean skipPastFound = false;

    for (TypeAdapterFactory factory : factories) {
      if (!skipPastFound) {
        if (factory == skipPast) {
          skipPastFound = true;
        }
        continue;
      }

      TypeAdapter<T> candidate = factory.create(this, type);
      if (candidate != null) {
        return candidate;
      }
    }
    throw new IllegalArgumentException("GSON cannot serialize " + type);
  }

  /**
   * Returns the type adapter for {@literal } type.
   *
   * @throws IllegalArgumentException if this GSON cannot serialize and
   *     deserialize {@literal type}.
   */
  public <T> TypeAdapter<T> getAdapter(Class<T> type) {
    return getAdapter(TypeToken.get(type));
  }

  public JsonElement toJsonTree(Object src) {
      return toJsonTree(src, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method serializes the specified object into its equivalent representation as a tree of
   * {@link JsonElement}s. This method should be used when the specified object is not a generic
   * type. This method uses {@link Class#getClass()} to get the type for the specified object, but
   * the {@literal getClass()} loses the generic type information because of the Type Erasure feature
   * of Java. Note that this method works fine if the any of the object fields are of generic type,
   * just the object itself should not be of a generic type. If the object is of generic type, use
   * {@link #toJsonTree(Object, Type)} instead.
   *
   * @param src the object for which Json representation is to be created setting for Jason
   * @return Json representation of {@literal src}.
   * @since 1.4
   */
  public JsonElement toJsonTree(Object src, RuntimeFilters runtimeFilters) {
    if (src == null) {
      return JsonNull.INSTANCE;
    }
    return toJsonTree(src, src.getClass(), runtimeFilters);
  }

  public JsonElement toJsonTree(Object src, Type typeOfSrc) {
      return toJsonTree(src, typeOfSrc, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method serializes the specified object, including those of generic types, into its
   * equivalent representation as a tree of {@link JsonElement}s. This method must be used if the
   * specified object is a generic type. For non-generic objects, use {@link #toJsonTree(Object)}
   * instead.
   *
   * @param src the object for which JSON representation is to be created
   * @param typeOfSrc The specific genericized type of src. You can obtain
   * this type by using the {@link com.google.gson.reflect.TypeToken} class. For example,
   * to get the type for {@literal Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfSrc = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @return Json representation of {@literal src}
   * @since 1.4
   */
  public JsonElement toJsonTree(Object src, Type typeOfSrc, RuntimeFilters runtimeFilters) {
    JsonTreeWriter writer = new JsonTreeWriter();
    toJson(src, typeOfSrc, writer, runtimeFilters);
    return writer.get();
  }

  public String toJson(Object src) {
      return toJson(src, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method serializes the specified object into its equivalent Json representation.
   * This method should be used when the specified object is not a generic type. This method uses
   * {@link Class#getClass()} to get the type for the specified object, but the
   * {@literal getClass()} loses the generic type information because of the Type Erasure feature
   * of Java. Note that this method works fine if the any of the object fields are of generic type,
   * just the object itself should not be of a generic type. If the object is of generic type, use
   * {@link #toJson(Object, Type)} instead. If you want to write out the object to a
   * {@link Writer}, use {@link #toJson(Object, Appendable)} instead.
   *
   * @param src the object for which Json representation is to be created setting for Jason
   * @return Json representation of {@literal src}.
   */
  public String toJson(Object src, RuntimeFilters runtimeFilters) {
    if (src == null) {
      return toJson(JsonNull.INSTANCE);
    }
    return toJson(src, src.getClass(), runtimeFilters);
  }

  public String toJson(Object src, Type typeOfSrc) {
      return toJson(src, typeOfSrc, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method serializes the specified object, including those of generic types, into its
   * equivalent Json representation. This method must be used if the specified object is a generic
   * type. For non-generic objects, use {@link #toJson(Object)} instead. If you want to write out
   * the object to a {@link Appendable}, use {@link #toJson(Object, Type, Appendable)} instead.
   *
   * @param src the object for which JSON representation is to be created
   * @param typeOfSrc The specific genericized type of src. You can obtain
   * this type by using the {@link com.google.gson.reflect.TypeToken} class. For example,
   * to get the type for {@literal Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfSrc = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @return Json representation of {@literal src}
   */
  public String toJson(Object src, Type typeOfSrc, RuntimeFilters runtimeFilters) {
    StringWriter writer = new StringWriter();
    toJson(src, typeOfSrc, writer, runtimeFilters);
    return writer.toString();
  }

  public void toJson(Object src, Appendable writer) throws JsonIOException {
      toJson(src, writer, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method serializes the specified object into its equivalent Json representation.
   * This method should be used when the specified object is not a generic type. This method uses
   * {@link Class#getClass()} to get the type for the specified object, but the
   * {@literal getClass()} loses the generic type information because of the Type Erasure feature
   * of Java. Note that this method works fine if the any of the object fields are of generic type,
   * just the object itself should not be of a generic type. If the object is of generic type, use
   * {@link #toJson(Object, Type, Appendable)} instead.
   *
   * @param src the object for which Json representation is to be created setting for Jason
   * @param writer Writer to which the Json representation needs to be written
   * @throws JsonIOException if there was a problem writing to the writer
   * @since 1.2
   */
  public void toJson(Object src, Appendable writer, RuntimeFilters runtimeFilters) throws JsonIOException {
    if (src != null) {
      toJson(src, src.getClass(), writer, runtimeFilters);
    } else {
      toJson(JsonNull.INSTANCE, writer);
    }
  }

  public void toJson(Object src, Type typeOfSrc, Appendable writer) throws JsonIOException {
      toJson(src, typeOfSrc, writer, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method serializes the specified object, including those of generic types, into its
   * equivalent Json representation. This method must be used if the specified object is a generic
   * type. For non-generic objects, use {@link #toJson(Object, Appendable)} instead.
   *
   * @param src the object for which JSON representation is to be created
   * @param typeOfSrc The specific genericized type of src. You can obtain
   * this type by using the {@link com.google.gson.reflect.TypeToken} class. For example,
   * to get the type for {@literal Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfSrc = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @param writer Writer to which the Json representation of src needs to be written.
   * @throws JsonIOException if there was a problem writing to the writer
   * @since 1.2
   */
  public void toJson(Object src, Type typeOfSrc, Appendable writer, RuntimeFilters runtimeFilters) throws JsonIOException {
    try {
      JsonWriter jsonWriter = newJsonWriter(Streams.writerForAppendable(writer));
      toJson(src, typeOfSrc, jsonWriter, runtimeFilters);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  public void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws JsonIOException {
      toJson(src, typeOfSrc, writer, EMPTY_RUNTIME_TRANSFORMER);
  }
  /**
   * Writes the JSON representation of {@literal src} of type {@literal typeOfSrc} to
   * {@literal writer}.
   * @throws JsonIOException if there was a problem writing to the writer
   */
  @SuppressWarnings("unchecked")
  public void toJson(Object src, Type typeOfSrc, JsonWriter writer, RuntimeFilters runtimeFilters) throws JsonIOException {
    TypeAdapter<?> adapter = getAdapter(TypeToken.get(typeOfSrc));
    boolean oldLenient = writer.isLenient();
    writer.setLenient(true);
    boolean oldHtmlSafe = writer.isHtmlSafe();
    writer.setHtmlSafe(htmlSafe);
    boolean oldSerializeNulls = writer.getSerializeNulls();
    writer.setSerializeNulls(serializeNulls);
    try {
      ((TypeAdapter<Object>) adapter).write(writer, src, runtimeFilters);
    } catch (IOException e) {
      throw new JsonIOException(e);
    } finally {
      writer.setLenient(oldLenient);
      writer.setHtmlSafe(oldHtmlSafe);
      writer.setSerializeNulls(oldSerializeNulls);
    }
  }

  public String toJson(JsonElement jsonElement) {
      return toJson(jsonElement, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * Converts a tree of {@link JsonElement}s into its equivalent JSON representation.
   *
   * @param jsonElement root of a tree of {@link JsonElement}s
   * @return JSON String representation of the tree
   * @since 1.4
   */
  public String toJson(JsonElement jsonElement, RuntimeFilters runtimeFilters) {
    StringWriter writer = new StringWriter();
    toJson(jsonElement, writer, runtimeFilters);
    return writer.toString();
  }

  /**
   * Writes out the equivalent JSON for a tree of {@link JsonElement}s.
   *
   * @param jsonElement root of a tree of {@link JsonElement}s
   * @param writer Writer to which the Json representation needs to be written
   * @throws JsonIOException if there was a problem writing to the writer
   * @since 1.4
   */
  public void toJson(JsonElement jsonElement, Appendable writer) throws JsonIOException {
    try {
      JsonWriter jsonWriter = newJsonWriter(Streams.writerForAppendable(writer));
      toJson(jsonElement, jsonWriter);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a new JSON writer configured for this GSON and with the non-execute
   * prefix if that is configured.
   */
  private JsonWriter newJsonWriter(Writer writer) throws IOException {
    if (generateNonExecutableJson) {
      writer.write(JSON_NON_EXECUTABLE_PREFIX);
    }
    JsonWriter jsonWriter = new JsonWriter(writer);
    if (prettyPrinting) {
      jsonWriter.setIndent("  ");
    }
    jsonWriter.setSerializeNulls(serializeNulls);
    return jsonWriter;
  }

  public void toJson(JsonElement jsonElement, JsonWriter writer) throws JsonIOException {
      toJson(jsonElement, writer, EMPTY_RUNTIME_TRANSFORMER);
  }
  /**
   * Writes the JSON for {@literal jsonElement} to {@literal writer}.
   * @throws JsonIOException if there was a problem writing to the writer
   */
  public void toJson(JsonElement jsonElement, JsonWriter writer, RuntimeFilters runtimeFilters) throws JsonIOException {
    boolean oldLenient = writer.isLenient();
    writer.setLenient(true);
    boolean oldHtmlSafe = writer.isHtmlSafe();
    writer.setHtmlSafe(htmlSafe);
    boolean oldSerializeNulls = writer.getSerializeNulls();
    writer.setSerializeNulls(serializeNulls);
    try {
      Streams.write(jsonElement, writer);
    } catch (IOException e) {
      throw new JsonIOException(e);
    } finally {
      writer.setLenient(oldLenient);
      writer.setHtmlSafe(oldHtmlSafe);
      writer.setSerializeNulls(oldSerializeNulls);
    }
  }

  public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
      return fromJson(json, classOfT, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method deserializes the specified Json into an object of the specified class. It is not
   * suitable to use if the specified class is a generic type since it will not have the generic
   * type information because of the Type Erasure feature of Java. Therefore, this method should not
   * be used if the desired type is a generic type. Note that this method works fine if the any of
   * the fields of the specified object are generics, just the object itself should not be a
   * generic type. For the cases when the object is of generic type, invoke
   * {@link #fromJson(String, Type)}. If you have the Json in a {@link Reader} instead of
   * a String, use {@link #fromJson(Reader, Class)} instead.
   *
   * @param <>> the type of the desired object
   * @param json the string from which the object is to be deserialized
   * @param classOfT the class of T
   * @return an object of type T from the string
   * @throws JsonSyntaxException if json is not a valid representation for an object of type
   * classOfT
   */
  public <T> T fromJson(String json, Class<T> classOfT, RuntimeFilters runtimeFilters) throws JsonSyntaxException {
    Object object = fromJson(json, (Type) classOfT, runtimeFilters);
    return Primitives.wrap(classOfT).cast(object);
  }

  public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
      return fromJson(json, typeOfT, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method deserializes the specified Json into an object of the specified type. This method
   * is useful if the specified object is a generic type. For non-generic objects, use
   * {@link #fromJson(String, Class)} instead. If you have the Json in a {@link Reader} instead of
   * a String, use {@link #fromJson(Reader, Type)} instead.
   *
   * @param <>> the type of the desired object
   * @param json the string from which the object is to be deserialized
   * @param typeOfT The specific genericized type of src. You can obtain this type by using the
   * {@link com.google.gson.reflect.TypeToken} class. For example, to get the type for
   * {@literal Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfT = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @return an object of type T from the string
   * @throws JsonParseException if json is not a valid representation for an object of type typeOfT
   * @throws JsonSyntaxException if json is not a valid representation for an object of type
   */
  @SuppressWarnings("unchecked")
  public <T> T fromJson(String json, Type typeOfT, RuntimeFilters runtimeFilters) throws JsonSyntaxException {
    if (json == null) {
      return null;
    }
    StringReader reader = new StringReader(json);
    T target = (T) fromJson(reader, typeOfT, runtimeFilters);
    return target;
  }

  public <T> T fromJson(Reader json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
      return fromJson(json, classOfT, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method deserializes the Json read from the specified reader into an object of the
   * specified class. It is not suitable to use if the specified class is a generic type since it
   * will not have the generic type information because of the Type Erasure feature of Java.
   * Therefore, this method should not be used if the desired type is a generic type. Note that
   * this method works fine if the any of the fields of the specified object are generics, just the
   * object itself should not be a generic type. For the cases when the object is of generic type,
   * invoke {@link #fromJson(Reader, Type)}. If you have the Json in a String form instead of a
   * {@link Reader}, use {@link #fromJson(String, Class)} instead.
   *
   * @param <>> the type of the desired object
   * @param json the reader producing the Json from which the object is to be deserialized.
   * @param classOfT the class of T
   * @return an object of type T from the string
   * @throws JsonIOException if there was a problem reading from the Reader
   * @throws JsonSyntaxException if json is not a valid representation for an object of type
   * @since 1.2
   */
  public <T> T fromJson(Reader json, Class<T> classOfT, RuntimeFilters runtimeFilters) throws JsonSyntaxException, JsonIOException {
    JsonReader jsonReader = new JsonReader(json);
    Object object = fromJson(jsonReader, classOfT);
    assertFullConsumption(object, jsonReader);
    return Primitives.wrap(classOfT).cast(object);
  }

  public <T> T fromJson(Reader json, Type typeOfT) throws JsonIOException, JsonSyntaxException {
      return fromJson(json, typeOfT, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method deserializes the Json read from the specified reader into an object of the
   * specified type. This method is useful if the specified object is a generic type. For
   * non-generic objects, use {@link #fromJson(Reader, Class)} instead. If you have the Json in a
   * String form instead of a {@link Reader}, use {@link #fromJson(String, Type)} instead.
   *
   * @param <>> the type of the desired object
   * @param json the reader producing Json from which the object is to be deserialized
   * @param typeOfT The specific genericized type of src. You can obtain this type by using the
   * {@link com.google.gson.reflect.TypeToken} class. For example, to get the type for
   * {@literal Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfT = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @return an object of type T from the json
   * @throws JsonIOException if there was a problem reading from the Reader
   * @throws JsonSyntaxException if json is not a valid representation for an object of type
   * @since 1.2
   */
  @SuppressWarnings("unchecked")
  public <T> T fromJson(Reader json, Type typeOfT, RuntimeFilters runtimeFilters) throws JsonIOException, JsonSyntaxException {
    JsonReader jsonReader = new JsonReader(json);
    T object = (T) fromJson(jsonReader, typeOfT, runtimeFilters);
    assertFullConsumption(object, jsonReader);
    return object;
  }

  private static void assertFullConsumption(Object obj, JsonReader reader) {
    try {
      if (obj != null && reader.peek() != JsonToken.END_DOCUMENT) {
        throw new JsonIOException("JSON document was not fully consumed.");
      }
    } catch (MalformedJsonException e) {
      throw new JsonSyntaxException(e);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  public <T> T fromJson(JsonReader reader, Type typeOfT) throws JsonIOException, JsonSyntaxException {
      return fromJson(reader, typeOfT, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * Reads the next JSON value from {@literal reader} and convert it to an object
   * of type {@literal typeOfT}.
   * Since Type is not parameterized by T, this method is type unsafe and should be used carefully
   *
   * @throws JsonIOException if there was a problem writing to the Reader
   * @throws JsonSyntaxException if json is not a valid representation for an object of type
   */
  @SuppressWarnings("unchecked")
  public <T> T fromJson(JsonReader reader, Type typeOfT, RuntimeFilters runtimeFilters) throws JsonIOException, JsonSyntaxException {
    boolean isEmpty = true;
    boolean oldLenient = reader.isLenient();
    reader.setLenient(true);
    try {
      reader.peek();
      isEmpty = false;
      TypeToken<T> typeToken = (TypeToken<T>) TypeToken.get(typeOfT);
      TypeAdapter<T> typeAdapter = getAdapter(typeToken);
      T object = typeAdapter.read(reader, runtimeFilters);
      return object;
    } catch (EOFException e) {
      /*
       * For compatibility with JSON 1.5 and earlier, we return null for empty
       * documents instead of throwing.
       */
      if (isEmpty) {
        return null;
      }
      throw new JsonSyntaxException(e);
    } catch (IllegalStateException e) {
      throw new JsonSyntaxException(e);
    } catch (IOException e) {
      // TODO(inder): Figure out whether it is indeed right to rethrow this as JsonSyntaxException
      throw new JsonSyntaxException(e);
    } finally {
      reader.setLenient(oldLenient);
    }
  }

  public <T> T fromJson(JsonElement json, Class<T> classOfT) throws JsonSyntaxException {
      return fromJson(json, classOfT, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method deserializes the Json read from the specified parse tree into an object of the
   * specified type. It is not suitable to use if the specified class is a generic type since it
   * will not have the generic type information because of the Type Erasure feature of Java.
   * Therefore, this method should not be used if the desired type is a generic type. Note that
   * this method works fine if the any of the fields of the specified object are generics, just the
   * object itself should not be a generic type. For the cases when the object is of generic type,
   * invoke {@link #fromJson(JsonElement, Type)}.
   * @param <>> the type of the desired object
   * @param json the root of the parse tree of {@link JsonElement}s from which the object is to
   * be deserialized
   * @param classOfT The class of T
   * @return an object of type T from the json
   * @throws JsonSyntaxException if json is not a valid representation for an object of type typeOfT
   * @since 1.3
   */
  public <T> T fromJson(JsonElement json, Class<T> classOfT, RuntimeFilters runtimeFilters) throws JsonSyntaxException {
    Object object = fromJson(json, (Type) classOfT, runtimeFilters);
    return Primitives.wrap(classOfT).cast(object);
  }

  public <T> T fromJson(JsonElement json, Type typeOfT) throws JsonSyntaxException {
      return fromJson(json, typeOfT, EMPTY_RUNTIME_TRANSFORMER);
  }

  /**
   * This method deserializes the Json read from the specified parse tree into an object of the
   * specified type. This method is useful if the specified object is a generic type. For
   * non-generic objects, use {@link #fromJson(JsonElement, Class)} instead.
   *
   * @param <>> the type of the desired object
   * @param json the root of the parse tree of {@link JsonElement}s from which the object is to
   * be deserialized
   * @param typeOfT The specific genericized type of src. You can obtain this type by using the
   * {@link com.google.gson.reflect.TypeToken} class. For example, to get the type for
   * {@literal Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfT = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @return an object of type T from the json
   * @throws JsonSyntaxException if json is not a valid representation for an object of type typeOfT
   * @since 1.3
   */
  @SuppressWarnings("unchecked")
  public <T> T fromJson(JsonElement json, Type typeOfT, RuntimeFilters runtimeFilters) throws JsonSyntaxException {
    if (json == null) {
      return null;
    }
    return (T) fromJson(new JsonTreeReader(json), typeOfT, runtimeFilters);
  }

  static class FutureTypeAdapter<T> extends TypeAdapter<T> {
    private TypeAdapter<T> delegate;

    public void setDelegate(TypeAdapter<T> typeAdapter) {
      if (delegate != null) {
        throw new AssertionError();
      }
      delegate = typeAdapter;
    }

    @Override public T read(JsonReader in, RuntimeFilters runtimeFilters) throws IOException {
      if (delegate == null) {
        throw new IllegalStateException();
      }
      return delegate.read(in, runtimeFilters);
    }

    @Override public void write(JsonWriter out, T value, RuntimeFilters runtimeFilters) throws IOException {
      if (delegate == null) {
        throw new IllegalStateException();
      }
      delegate.write(out, value, runtimeFilters);
    }
  }

  @Override
  public String toString() {
  	return new StringBuilder("{serializeNulls:")
  	    .append(serializeNulls)
  	    .append("factories:").append(factories)
        .append(",instanceCreators:").append(constructorConstructor)
        .append("}")
        .toString();
  }
}
