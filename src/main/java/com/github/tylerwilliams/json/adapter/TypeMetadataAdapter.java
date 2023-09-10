package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonToken;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;

/**
 * Adapter which will write implementation metadata to allow interfaces and abstract classes to be serialized
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TypeMetadataAdapter implements JsonAdapter<Object> {

    private static final JsonAdapter<Object> INSTANCE = new TypeMetadataAdapter();

    public static JsonAdapter<Object> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, Object obj) throws IOException {
        if (obj == null) {
            writer.writeNull();
            return;
        }

        writer.writeStartArray();

        writer.writeString(obj.getClass().getName());

        JsonAdapter implAdapter = writer.getSerializationContext().getAdapter(obj.getClass());
        if (implAdapter instanceof TypeMetadataAdapter) {
            implAdapter = ObjectAdapter.getInstance();
        }
        implAdapter.writeObject(writer, obj);

        writer.writeEndArray();
    }

    @Override
    public Object readObject(JsonReader jsonReader, JavaType type) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.readNull();
            return null;
        }

        jsonReader.readStartArray();

        String implClassName = jsonReader.readString();
        Class<?> implClass;
        try {
            implClass = Class.forName(implClassName);
        } catch (ClassNotFoundException e) {
            throw jsonReader.createJsonBindException("Could not lookup class '" + implClassName + "'");
        }

        JsonAdapter implAdapter = jsonReader.getSerializationContext().getAdapter(implClass);
        if (implAdapter instanceof TypeMetadataAdapter) {
            implAdapter = ObjectAdapter.getInstance();
        }

        Object impl = implAdapter.readObject(jsonReader, JavaType.from(implClass));
        jsonReader.readEndArray();
        return impl;
    }

}
