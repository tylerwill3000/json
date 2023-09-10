package com.github.tylerwilliams.json.util

import spock.lang.Specification

class TypeRegistrySpec extends Specification {

    static interface Interface {}

    static class Parent {}

    static class InterfaceParent implements Interface {}

    static class Child extends Parent {}

    static class InterfaceChild extends Parent implements Interface {}

    static class GrandChild extends Child {}

    static class InterfaceGrandChild extends Child implements Interface {}

    def 'when looking up a subclass entry that is not directly present, you will receive the superclass entry'() {
        setup:
            def registry = new TypeRegistry().register(Parent, 'parent')

        expect:
            registry.get(Child) == 'parent'
    }

    def 'when both a superclass and superinterface entry are present at the same level, the superclass entry takes precedence'() {
        setup:
            def registry = new TypeRegistry().register(Parent, 'parent').register(Interface, 'interface')

        expect:
            registry.get(InterfaceChild) == 'parent'
    }

    def 'when only an interface is present, the interface will take precedence over the object superclass'() {
        setup:
            def registry = new TypeRegistry().register(Object, 'object').register(Interface, 'interface')

        expect:
            registry.get(InterfaceParent) == 'interface'
    }

    def 'inserting a closer superclass will invalidate the old mapped subclass value'() {
        setup:
            def registry = new TypeRegistry().register(Parent, 'parent')
            registry.get(GrandChild)

        when:
            registry.register(Child, 'child')

        then:
            registry.get(GrandChild) == 'child'
    }

    def 'adding an explicit mapping after a superclass mapping has already been indexed'() {
        setup:
            def registry = new TypeRegistry().register(Parent, 'parent')
            registry.get(Child)

        when:
            registry.register(Child, 'child')

        then:
            registry.get(Child) == 'child'
    }

    def 'both primitive and reference array types map to Object[]'() {
        setup:
            def registry = new TypeRegistry().register(Object[], 'object array')

        expect:
            registry.get(int[]) == 'object array'
            registry.get(String[]) == 'object array'
            registry.get(String[][]) == 'object array'
    }

    def 'class distance'() {
        expect:
            TypeRegistry.classDistance(Child, Child) == 0
            TypeRegistry.classDistance(Child, Parent) == 1
            TypeRegistry.classDistance(GrandChild, Parent) == 2
            TypeRegistry.classDistance(InterfaceChild, Interface) == 1
    }

}
