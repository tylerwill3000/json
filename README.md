# Simple JSON

This is a lightweight library to serialize and de-serialize JSON to and from java objects.

# Features
- No external dependencies
- Supports serialization and deserialization of arbitrarily complex generic signatures (e.g. `List<Map<URL, String>>`)
- Speed on par with both [GSON](https://github.com/google/gson) and [Jackson](https://github.com/FasterXML/jackson). The library is SLIGHTLY slower on deserialization, but the slowdown can only be seen when reading tens of thousands of objects
- Detection of cyclic references
- Ability to support single line and multi-line comments when parsing
- Ability to serialize and deserialize interfaces and abstract classes
- Ability to use constructors for deserialization, allowing to deserialize immutable classes with final fields

# Basic Usage
The API entry point is the `Json` class. Once an instance is created, it can be reused for parsing and serializing:

```java
class Person {
  String name;
  int age;
  
  Person(String name, int age) {
    this.name = name;
    this.age = age;
  }
}

var json = new Json();
json.serialize(new Person("Jason", 35)); // {"name":"Jason","age":35}

var parsedPerson = json.parse("{\"name\":\"Bobby\",\"age\":25}", Person.class);
assert parsedPerson.name.equals("Bobby");
assert parsedPerson.age == 25;
```

By default, ```Json``` will serialize all fields using their java field names. To exclude a field, you can annotate it with ```@JsonIgnore```.
```java
class Person {
  String name;
  int age;
  
  @JsonIgnore
  String ssn;
}

var json = new Json();
json.serialize(new Person("Jason", 35, "123-456-7890")); // {"name":"Jason","age":35}
```

Null values, by default, will be skipped during serialization:
```java
var json = new Json();
json.serialize(new Person(null, 85)); // {\"age\":85}
```

On the deserialization side of things, ```Json``` will throw an error if it encounters any fields in the JSON string which it cannot map to a java field. To allow unknown fields to be skipped during deserialization, annotate the class with ```@JsonIgnoreExtraFields``` 
```java
class Person {
  String name;
  int age;
}

@JsonIgnoreExtraFields
class PersonIgnoreFields {
  String name;
  int age;
}

var json = new Json();

// This will throw an error, since 'gender' is not mapped to a field!
json.parse("{\"name\":\"Jason\",\"age\":35,\"gender\":\"male\"}", Person.class);

// This will succeed
json.parse("{\"name\":\"Jason\",\"age\":35,\"gender\":\"male\"}", PersonIgnoreFields.class);
```

# Deserializing Generic Types

One of the goals of this library is to be able to read JSON structures into arbitrarily complex generic types.

Because of java's type erasure, this must be done by passing an instance of a special class called a `TypeToken` into your parsing method. The type token contains the generic signature you wish to parse the JSON as:

```java
String jsonStr =
  "[" +
    "{\"name\":\"Jason\"}," +
    "{\"name\":\"Billy\"}" +
  "]"; 

Type listOfMaps = new TypeToken<List<Map<String, String>>>(){}.getType();

var json = new Json();
List<Map<String, String>> parsed = json.parse(jsonStr, listOfMaps);
```

The generic type signature can be as deep or complex as your situation requires (however, after a certain point, you should really consider just creating a dedicated class!)

# Custom Serialization

To register a custom serializer for a given type, you can do the following:
```java
var json = new Json();
json.registerAdapter(Person.class, new JsonAdapter<>() {
  
   @Override
   public void writeObject(JsonWriter writer, Person person) throws IOException {
     jsonWriter.writeString(person.name + "|" + person.age);
   }

   @Override
   public Person readObject(JsonReader jsonReader, JavaType<? extends Person> type) throws IOException {
     String personSplit = jsonReader.readString().split("\\|");
     String name = personSplit[0];
     int age = Integer.parseInt(personSplit[1]);
     return new Person(name, age);
   }
   
});

List<Person> people = List.of(new Person("Jason", 25), new Person("Bobby", 35));
json.serialize(people); // ["Jason|25", "Bobby|35"]

List<Person> parsedPeople = json.parse("[\"Alice|43\",\"Bob|36\"]", new TypeToken<List<Person>>(){}.getType());
assert parsedPeople.size() == 2
assert parsedPeople.get(0).name.equals("Alice");
assert parsedPeople.get(0).age == 43;
assert parsedPeople.get(1).name.equals("Bob");
assert parsedPeople.get(1).age == 36;
```

Usage of these low-level custom serializers should be relatively rare since the library comes with serializers for most built-in types, and common customizations can be applied using annotations.

Built-in serializers are provided for all of the following:
- Primitive types and their wrappers
- Arrays, collections and maps
- Enums
- `String`
- `BigDecimal` and `BigInteger`
- `Locale`
- `Currency`
- `InetAddress`
- Java 8 date & time classes
- `java.util.Date` & `Calendar`
- `File` and `Path`
- `UUID`
- `URL` and `URI`
 
 If an appropriate serializer is not found, it will simply be serialized into a JSON object using reflection.
 
# Abstract classes & interfaces

The library can serialize abstract classes and interfaces by writing type metadata to the JSON stream along with the actual object value. This type metadata is then used when reading the value to determine which implementation class to use.

You must opt-in to this feature for each class you wish to use it for. This can be done by registering the `TypeMetadataAdapter` as a custom adapter for your interface / abstract type:

```java
interface MyInterface {}

class MyImplA implements MyInterface {
  int implAField;
}

class MyImplB implements MyInterface {
  int implBField;
}

var json = new Json();
json.registerAdapter(MyInterface.class, TypeMetadataAdapter.getInstance());

json.serialize(new MyImplA(1)); // ["my.package.MyImplA",{"implAField":1}]
json.serialize(new MyImplB(2)); // ["my.package.MyImplB",{"implBField":2}]
```