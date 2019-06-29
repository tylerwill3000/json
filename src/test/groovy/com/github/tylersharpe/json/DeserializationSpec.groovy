package com.github.tylersharpe.json

import com.github.tylersharpe.json.adapter.JsonAdapter
import com.github.tylersharpe.json.annotation.JsonConstructor
import com.github.tylersharpe.json.annotation.JsonIgnoreExtraFields
import com.github.tylersharpe.json.annotation.JsonProperty
import com.github.tylersharpe.json.annotation.JsonSerialization
import com.github.tylersharpe.json.util.JavaType
import com.github.tylersharpe.json.util.TypeToken
import groovy.transform.Canonical
import spock.lang.Specification

import java.lang.reflect.Type
import java.sql.Timestamp

class DeserializationSpec extends Specification {

  Json json = new Json()

  def 'read JSON array with only primitives'() {
    expect:
    json.parse('[1,"hello","world",false]') == [1, 'hello', 'world', false]
  }

  def 'read JSON array with primitives and complex objects'() {
    expect:
    json.parse('[1,{"hello":"world","jsonAwesome":true},false]') == [1, [hello: 'world', jsonAwesome: true], false]
  }

  def 'read JSON object with only primitives'() {
    expect:
    json.parse('{"name":"tester","admin":false,"age":20}') == [name: 'tester', admin: false, age: 20]
  }

  def 'read JSON object with primitive and complex values'() {
    expect:
    json.parse('{"name":"tester","roles":{"admin":false,"tester":true},"age":20}') == [
            name: 'tester',
            roles: [admin: false, tester: true],
            age: 20
    ]
  }

  def 'read JSON array using interface type'() {
    expect:
    json.parse('[1,2,3]', Collection) == [1,2,3]
  }

  def 'read JSON array using concrete type'() {
    expect:
    json.parse('[1,2,3]', ArrayList) == [1,2,3] as ArrayList
  }

  def 'read JSON array to primitive array type'() {
    expect:
    json.parse('[1,2,3]', int[]) == [1,2,3] as int[]
  }

  def 'read JSON array to object array type'() {
    expect:
    json.parse('[1,2,3]', Integer[]) == [1,2,3] as Integer[]
  }

  def 'premature end of stream should throw EOFException'() {
    when:
    json.parse '[1,2,3'

    then:
    thrown EOFException
  }

  def 'read negative numbers'() {
    when:
    def result = (List) json.parse('[-1,-1.2,-.4]')

    then:
    result[0] == new BigInteger('-1')
    result[1] == new BigDecimal('-1.2')
    result[2] == new BigDecimal('-.4')
  }

  def 'error is thrown if there are no entries before the first comma in an array'() {
    when:
    json.parse '[,1,3]'

    then:
    def ex = thrown(MalformedJsonException)
    ex.message == 'Cannot read a new element starting from token COMMA (near index: 0)'
  }

  def 'read JSON which has escaped quotes'() {
    expect:
    json.parse('{"value":"Hello \\"friend\\""}') == [value: 'Hello "friend"']
  }

  def 'error is thrown if there is no colon after an object property'() {
    when:
    json.parse '{"name"}'

    then:
    def ex = thrown(MalformedJsonException)
    ex.message == "Expected ':' character but instead found '}' (near index: 7)"
  }

  @Canonical
  static class Person {
    String name
    int age
    boolean employed
  }
  def 'read java class'() {
    expect:
    json.parse('{"name":"Tester","age":20,"employed":true}', Person) == new Person(name: 'Tester', age: 20, employed: true)
  }

  static class NameAdapter implements JsonAdapter<String> {

    @Override
    void writeObject(JsonWriter jsonWriter, String obj) throws IOException {
      jsonWriter.writeValue("custom:" + obj)
    }

    @Override
    String readObject(JsonReader jsonReader, JavaType<? extends String> type) throws IOException {
      jsonReader.readString().substring('custom:'.length())
    }
  }

  @Canonical
  static class PersonWithSerializer {
    @JsonSerialization(NameAdapter)
    String name
  }
  def 'read a java class which uses a custom adapter for a specific field'() {
    expect:
    json.parse('{"name":"custom:tester"}', PersonWithSerializer) == new PersonWithSerializer(name: 'tester')
  }

  @Canonical
  static class PersonWithCustomProp {
    @JsonProperty("customName") String name
    int age
  }
  def 'read java class when a field has a custom property name'() {
    expect:
    json.parse('{"customName":"Tester","age":20}', PersonWithCustomProp) == new PersonWithCustomProp(name: 'Tester', age: 20)
  }

  @Canonical
  static class ImmutableClass {
    private final String finalString
    private final int finalInt

    @JsonConstructor
    ImmutableClass(@JsonProperty("finalString") String finalString, @JsonProperty("finalInt") int finalInt) {
      this.finalString = finalString
      this.finalInt = finalInt
    }
  }
  def 'read java class which uses a constructor for instance creation'() {
    expect:
    json.parse('{"finalString":"abc","finalInt":1}', ImmutableClass) == new ImmutableClass('abc', 1)
  }

  def 'read java class which uses a constructor for instance creation when the JSON field order does not match the constructor field order'() {
    expect:
    json.parse('{"finalInt":1,"finalString":"abc"}', ImmutableClass) == new ImmutableClass('abc', 1)
  }

  @Canonical
  static class DateClass { Date date }
  def 'read date value'() {
    expect:
    json.parse('{"date":"2010-10-5T10:50:12"}', DateClass) == new DateClass(date: new Date(110, 9, 5, 10, 50, 12))
  }

  @Canonical
  static class TimestampClass { Timestamp timestamp }
  def 'read timestamp value'() {
    given:
    def expectTimestamp = new Timestamp(new Date(110, 9, 5, 10, 50, 12).getTime())

    expect:
    json.parse('{"timestamp":"2010-10-5T10:50:12"}', TimestampClass) == new TimestampClass(timestamp: expectTimestamp)
  }

  @Canonical
  static class CalendarClass { Calendar calendar }
  def 'read calendar value'() {
    setup:
    def expectCal = Calendar.instance
    expectCal.time = new Date(110, 9, 5, 10, 50, 12)

    expect:
    json.parse('{"calendar":"2010-10-5T10:50:12"}', CalendarClass) == new CalendarClass(calendar: expectCal)
  }

  @Canonical
  @JsonIgnoreExtraFields
  static class MissingSomeProperties {
    int age
  }
  def 'extra JSON properties are ignored if the bind class is annotated to allow this'() {
    expect:
    json.parse('{"age":10,"name":"John"}', MissingSomeProperties) == new MissingSomeProperties(age: 10)
  }

  @Canonical
  static class Id {
    @SuppressWarnings("unused") int id
  }
  def "read collection using the collection's generic type to deserialize each item"() {
    setup:
    Type type = new TypeToken<List<Id>>(){}.type

    expect:
    json.parse('[{"id":1},{"id":2},{"id":3}]', type) == [new Id(1), new Id(2), new Id(3)]
  }

  @Canonical
  static class Student {
    int age
    String name
    double gpa
  }
  def 'read object in which a value is null'() {
    expect:
    json.parse('{"age":25,"name":null,"gpa":4.2}', Student) == new Student(age: 25, gpa: 4.2, name: null)
  }

  static enum MediaType { BOOKS, MOVIES, STREAMING }
  def 'read object using deeply nested generic types'() {
    setup:
    def typeToken = new TypeToken<List<Map<MediaType, LinkedList<URL>>>>(){}.type
    def jsonStr = """
      [
        {
          "MOVIES": ["https://www.thepiratebay.org", "https://rarbg.to"],
          "BOOKS": ["https://www.audiobookbay.org"]
        },
        {
          "STREAMING": ["https://www.youtube.com", "https://twitch.tv"]
        }
      ]
    """

    when:
    def result = json.parse(jsonStr, typeToken)

    then:
    result instanceof List
    result.every { it instanceof Map }
    Map map = (Map) ((List) result).get(0)
    map.containsKey(MediaType.MOVIES)
    map.containsKey(MediaType.BOOKS)
    def moviesList = map[MediaType.MOVIES]
    moviesList instanceof LinkedList
    moviesList.every { it instanceof URL }
  }

  static interface Greeter {}
  def 'an error is thrown when trying to parse an interface type and no explicit adapter is registered'() {
    when:
    json.parse('{"name":"Bobby"}', Greeter)

    then:
    def ex = thrown(JsonBindException)
    ex.message.contains "Cannot directly instantiate $Greeter"
  }

  def 'single line comments can be ignored'() {
    given:
    json.allowComments = true
    def jsonStr = """
      {
        // Comment explaining config value
        "prop": "value"
      }
    """

    expect:
    json.parse(jsonStr) == [prop: 'value']
  }

  def 'multiple single line comments in a row can be ignored'() {
    given:
    json.allowComments = true
    def jsonStr = """
      {
        // Comment explaining config value
        // Pay attention!
        "prop": "value"
      }
    """

    expect:
    json.parse(jsonStr) == [prop: 'value']
  }

  def 'multi-line comments can be ignored'() {
    given:
    json.allowComments = true
    def jsonStr = """
      {
        /*
          Here is some important info to read!
        */
        "prop": "value"
      }
    """

    expect:
    json.parse(jsonStr) == [prop: 'value']
  }

  def 'reading a wildcard type that has a concrete upper bound class should read items as the upper bound class'() {
    given:
    Type listOfIdType = new TypeToken<List<? extends Id>>(){}.type

    when:
    def result = json.parse('[{"id":1},{"id":2}]', listOfIdType)

    then:
    result.every { it instanceof Id }
  }

  def 'reading a wildcard type that has a concrete lower bound class should read items as the lower bound class'() {
    given:
    Type listOfIdType = new TypeToken<List<? super Id>>(){}.type

    when:
    def result = json.parse('[{"id":1},{"id":2}]', listOfIdType)

    then:
    result.every { it instanceof Id }
  }

}
