package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;
import com.github.tylerwilliams.json.JsonToken;

import java.io.IOException;

/**
 * Serializes and de-serializes object types to / create a {@link JsonReader} and {@link JsonWriter}
 */
public interface JsonAdapter<T> {

    void writeObject(JsonWriter writer, T obj) throws IOException;

    T readObject(JsonReader jsonReader, JavaType<? extends T> type) throws IOException;

    default JsonAdapter<T> nullSafe() {
        JsonAdapter<T> wrapped = this;

        return new JsonAdapter<>() {
            @Override
            public void writeObject(JsonWriter writer, T obj) throws IOException {
                if (obj == null) {
                    writer.writeNull();
                } else {
                    wrapped.writeObject(writer, obj);
                }
            }

            @Override
            public T readObject(JsonReader reader, JavaType<? extends T> type) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.readNull();
                    return null;
                }
                return wrapped.readObject(reader, type);
            }
        };
    }

}