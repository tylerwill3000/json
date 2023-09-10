package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayAdapter implements JsonAdapter<Object> {

    private static final JsonAdapter<Object> INSTANCE = new ArrayAdapter().nullSafe();

    private ArrayAdapter() {
    }

    public static JsonAdapter<Object> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, Object array) throws IOException {
        writer.writeStartArray();

        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object arrayItem = Array.get(array, i);
            writer.writeValue(arrayItem);
        }

        writer.writeEndArray();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object readObject(JsonReader reader, JavaType<?> type) throws IOException {
        Class arrayItemType = type.getRawType().getComponentType();

        List list = new ArrayList();
        reader.iterateNextArray(() -> {
            var deserializedItem = reader.readClass(arrayItemType);
            list.add(deserializedItem);
        });

        var array = Array.newInstance(arrayItemType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, list.get(i));
        }
        return array;
    }

}
