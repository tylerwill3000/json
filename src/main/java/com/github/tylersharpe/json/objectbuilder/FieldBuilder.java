package com.github.tylersharpe.json.objectbuilder;

import com.github.tylersharpe.json.JsonBindException;
import com.github.tylersharpe.json.JsonReader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Builds objects using 'java bean' style construction (invoke no-arg constructor and set fields 1 by 1)
 */
public class FieldBuilder<T> implements ObjectBuilder<T> {

    private T instance;

    FieldBuilder(Class<? extends T> clazz) {
        try {
            this.instance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new JsonBindException(e);
        }
    }

    @Override
    public void accumulateField(JsonReader reader) throws IOException {
        String jsonProperty = reader.readKey();
        Field field = ObjectBuilder.findFieldForJsonProperty(instance.getClass(), jsonProperty);

        if (field == null) {
            reader.readClass(Object.class);
            return;
        }

        Object fieldValue = ObjectBuilder.readFieldValue(reader, field);

        field.setAccessible(true);
        try {
            field.set(instance, fieldValue);
        } catch (IllegalAccessException e) {
            throw new JsonBindException(e);
        }
    }

    @Override
    public T buildObject() {
        return instance;
    }

}
