package com.github.tylersharpe.json

import com.github.tylersharpe.json.adapter.JsonAdapter
import com.github.tylersharpe.json.annotation.JsonProperty
import com.github.tylersharpe.json.annotation.JsonSerialization
import com.github.tylersharpe.json.util.JavaType
import groovy.transform.Canonical
import spock.lang.Specification

import java.time.*
import java.util.concurrent.TimeUnit

class SerializationSpec extends Specification {

    Json json = new Json()

    def 'write JSON array with primitive items'() {
        expect:
            json.serialize(['hello', 'world', '!']) == '["hello","world","!"]'
    }

    def 'write JSON array with complex items'() {
        expect:
            json.serialize(['hello', ['nested'], 'world']) == '["hello",["nested"],"world"]'
    }

    def 'write JSON object with primitive items'() {
        expect:
            json.serialize([hello: 'world', testing: 'rocks']) == '{"hello":"world","testing":"rocks"}'
    }

    def 'write JSON object with primitive items pretty'() {
        given:
            json.whitespaceWriter = PrettyWhitespaceWriter.withIndent(2)

        expect:
            json.serialize([hello: 'world', testing: 'rocks']) == """{\n  "hello": "world",\n  "testing": "rocks"\n}"""
    }

    def 'write JSON object with complex items'() {
        given:
            def toWrite = [
                    hello       : 'world',
                    nestedArray : [1, true, 'string'],
                    testing     : 'rocks',
                    nestedObject: [name: 'John', age: 25]
            ]

        expect:
            json.serialize(toWrite) == '{"hello":"world","nestedArray":[1,true,"string"],"testing":"rocks","nestedObject":{"name":"John","age":25}}'
    }

    def 'quotes are escaped when writing strings that have quotes in them'() {
        expect:
            json.serialize(['k"ey': 'Hello "friend"']) == '{"k\\"ey":"Hello \\"friend\\""}'
    }

    static class ClassWithJsonProperty {
        @JsonProperty("firstName")
        String name
    }

    def '@JsonProperty controls the names of fields'() {
        expect:
            json.serialize(new ClassWithJsonProperty(name: 'tester')) == '{"firstName":"tester"}'
    }

    static class A {
        B b
    }

    static class B {
        C c
    }

    static class C {
        A a
    }

    def 'an exception is thrown when a cyclic reference is detected'() {
        given:
            A a = new A()
            B b = new B()
            C c = new C()
            a.b = b
            b.c = c
            c.a = a

        when:
            json.serialize(a)

        then:
            def ex = thrown CyclicReferenceException
            ex.message == "Cyclic reference to $A detected in $C\nThrough reference chain:\n  $A\n  $B\n  $C"
    }

    static class Parent {
        int parentField
    }

    static class Child extends Parent {
        int childField
    }

    def 'superclass fields are written'() {
        expect:
            json.serialize(new Child(childField: 1, parentField: 2)) == '{"childField":1,"parentField":2}'
    }

    class Inner {
        int field
    }

    def "the parent scope 'this' value is not serialized for non-static inner classes"() {
        expect:
            json.serialize(new Inner(field: 1)) == '{"field":1}'
    }

    static class DateClass {
        LocalDate localDate
        LocalTime localTime
        LocalDateTime localDateTime
        ZonedDateTime zonedDateTime
        OffsetDateTime offsetDateTime
        OffsetTime offsetTime
        Year year
        YearMonth yearMonth
    }

    def 'write date fields'() {
        when:
            def result = json.serialize(new DateClass(
                    localDate: LocalDate.of(2008, 10, 12),
                    localTime: LocalTime.of(8, 30, 30),
                    localDateTime: LocalDateTime.of(2008, 10, 12, 5, 12, 30, (int) TimeUnit.MILLISECONDS.toNanos(355)),
                    zonedDateTime: ZonedDateTime.of(2008, 10, 12, 3, 12, 30, 0, ZoneId.of("Europe/Paris")),
                    offsetDateTime: OffsetDateTime.of(2008, 10, 12, 3, 12, 30, 0, ZoneOffset.ofHours(-4)),
                    offsetTime: OffsetTime.of(7, 28, 50, 0, ZoneOffset.ofHours(3)),
                    year: Year.of(1995),
                    yearMonth: YearMonth.of(1995, Month.APRIL)
            ))

        then:
            result == "{" +
                    '"localDate":"2008-10-12",' +
                    '"localTime":"08:30:30",' +
                    '"localDateTime":"2008-10-12T05:12:30.355",' +
                    '"zonedDateTime":"2008-10-12T03:12:30+02:00[Europe/Paris]",' +
                    '"offsetDateTime":"2008-10-12T03:12:30-04:00",' +
                    '"offsetTime":"07:28:50+03:00",' +
                    '"year":"1995",' +
                    '"yearMonth":"1995-04"' +
                    "}"
    }

    static class NameAdapter implements JsonAdapter<String> {

        @Override
        void writeObject(JsonWriter writer, String obj) throws IOException {
            writer.writeValue("custom:" + obj)
        }

        @Override
        String readObject(JsonReader jsonReader, JavaType<? extends String> type) throws IOException {
            jsonReader.readString().substring('custom:'.length())
        }
    }

    @Canonical
    static class CustomSerializerClass {
        @JsonSerialization(NameAdapter)
        String name
    }

    def 'write field using custom serializer'() {
        expect:
            json.serialize(new CustomSerializerClass(name: 'tester')) == '{"name":"custom:tester"}'
    }

    @Canonical
    static class NullsClass {
        @SuppressWarnings("unused")
        String name
    }

    def 'nulls are serialized if the JSON instance is configured to do so'() {
        given:
            json.serializeNulls = true

        expect:
            json.serialize(new NullsClass()) == '{"name":null}'
    }

}