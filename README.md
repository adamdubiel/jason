# Jason

Jason is an elastic JSON serializer/deserializer based on Google Gson 2.2.4 code.
For more on Gson visit [Google Code page](http://code.google.com/p/google-gson).

## Why?

Gson is a great, performant tool with great architecture. There is one thing missing
though - runtime filtering. Filters (or excluders) need to be defined up front,
before `gson` object is built. Jason aims at bringing runtime excluders passed
on serialize/deserialize method call.

This project is here to show that it is possible. I will try to contact Gson authors to merge code.

## What's new?

* runtime transformations
* serialization using getters

## How to use it?

### Runtime transformation

To define runtime filter that will allow **only** *name* fields:

```java
// given
AttributeFilter attributeFilter = new AttributeFilter().including("name");
RuntimeTransformer transformer = new RuntimeTransformer(attributeFilter);

DummyClass dummy = DummyClassBuilder.dummyClass().withName("dummy").build();

// when
Strign json = gson.toJson(dummy, transformer);

// then
assertThat(json).isEqualTo("{name: \"dummy\"}");
```

Runtime filter that will restrict fields only on chosen class:

```java
// given
AttributeFilter attributeFilter = new AttributeFilter().including(DummyClass.class, "name", "id");
RuntimeTransformer transformer = new RuntimeTransformer(attributeFilter);

DummyClass dummy = DummyClassBuilder.dummyClass().withName("dummy").withId(42).build();
Aggregate aggregate = AggregateBuilder.aggregate().withName("aggregate").contains(dummy);

// when
Strign json = gson.toJson(aggregate, transformer);

// then
assertThat(json).isEqualTo("{name: \"aggregate\" content: [{ name: \"dummy\", id: 42}] }");
```

#### Attribute filters

Attribute filters allow to perform global and local exclusions based on field name only. Runtime
transformations are dynamic and should not rely on complex information about field (like annotations).
Any complex exclusions should be handled via ExclusionStrategy.

Attribute filtering precedence:

* allow all by default
* global exclusion can be overriden only by class level include
* include on class level overrides global includes
* exclude on class level adds up to global excludes

### Serialization using getters

By default getter serialization can be turned on using `@JasonAccess(strategy = AccessStrategyType.PROPERTY)` annotation
on class level. You can define own access strategy by providing own implementation of `AccessStrategy`.

**Warning!** There is no support for deserialization. Classes which are serialized using getters **can't be deserialized**!

## Compatibility

The only thing that needs to be changed after moving to Jason is `ExclusionStrategy` implementations signature. Instead
of `FieldAttributes` interface `skipField` method now accepts `Attribute`, which can be either field or property (getter).
`Attribute` interface offers same possibilities as `FieldAttribute`.