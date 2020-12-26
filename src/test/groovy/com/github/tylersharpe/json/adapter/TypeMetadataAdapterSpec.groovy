package com.github.tylersharpe.json.adapter

import com.github.tylersharpe.json.Json
import com.github.tylersharpe.json.JsonReader
import com.github.tylersharpe.json.JsonWriter
import com.github.tylersharpe.json.util.JavaType
import spock.lang.Specification

class TypeMetadataAdapterSpec extends Specification {

    static interface MyInterface {}

    static class MyImplA implements MyInterface {
        int implAField
    }

    static class MyImplB implements MyInterface {
        int implBField
    }

    static class MyImplC implements MyInterface {
        String name
    }

    static class MyObj {
        MyInterface interfaceField
    }

    Json json = new Json()

    def 'interface fields can be read and written'() {
        given: 'json instance with registry that knows how to read and write interfaces'
            json.registerAdapter(MyInterface, TypeMetadataAdapter.instance)

        when: 'interface instance is written'
            def interfaceOutput = json.serialize(new MyImplA(implAField: 10))

        then: 'output contains appropriate metadata'
            interfaceOutput == """["${MyImplA.name}",{"implAField":10}]"""

        when: 'interface instance is read'
            def parsed = json.parse("""["${MyImplB.name}",{"implBField":20}]""", MyInterface)

        then: 'appropriate implementation is returned'
            parsed instanceof MyImplB
            ((MyImplB) parsed).implBField == 20
    }

    def 'custom serializers are used when reading and writing the impl class'() {
        given: 'json instance with appropriate registry'
            json.registerAdapter(MyInterface, TypeMetadataAdapter.instance)
            json.registerAdapter(MyImplC, new JsonAdapter<MyImplC>() {
                @Override
                void writeObject(JsonWriter writer, MyImplC obj) throws IOException {
                    writer.writeString("custom:${obj.name}")
                }

                @Override
                MyImplC readObject(JsonReader reader, JavaType<? extends MyImplC> type) throws IOException {
                    def name = reader.readString().substring('custom:'.length())
                    return new MyImplC(name: name)
                }
            })

        when: 'the object is written'
            def interfaceOutput = json.serialize(new MyObj(interfaceField: new MyImplC(name: 'writeTest')))

        then: 'output corresponds to the custom serializer'
            interfaceOutput == """{"interfaceField":["${MyImplC.name}","custom:writeTest"]}"""

        when: 'interface instance is read'
            MyInterface parsed = json.parse("""{"interfaceField":["${MyImplC.name}","custom:readTest"]}""", MyObj).interfaceField

        then: 'appropriate implementation is returned'
            parsed instanceof MyImplC
            ((MyImplC) parsed).name == 'readTest'
    }

}
