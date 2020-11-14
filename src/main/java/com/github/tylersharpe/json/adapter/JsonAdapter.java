package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;

import static com.github.tylersharpe.json.JsonToken.NULL;

/**
 * Serializes and de-serializes object types to / create a {@link JsonReader} and {@link JsonWriter}
 */
public interface JsonAdapter<T> {

    void writeObject(JsonWriter jsonWriter, T obj) throws IOException;

    T readObject(JsonReader jsonReader, JavaType<? extends T> type) throws IOException;

    default JsonAdapter<T> nullSafe() {
        JsonAdapter<T> wrapped = this;

        return new JsonAdapter<>() {
            @Override
            public void writeObject(JsonWriter jsonWriter, T obj) throws IOException {
                if (obj == null) {
                    jsonWriter.writeNull();
                } else {
                    wrapped.writeObject(jsonWriter, obj);
                }
            }

            @Override
            public T readObject(JsonReader reader, JavaType<? extends T> type) throws IOException {
                if (reader.peek() == NULL) {
                    reader.readNull();
                    return null;
                }
                return wrapped.readObject(reader, type);
            }
        };
    }

}