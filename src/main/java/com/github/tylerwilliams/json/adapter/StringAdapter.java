package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;

public class StringAdapter implements JsonAdapter<String> {

    private static final JsonAdapter<String> INSTANCE = new StringAdapter().nullSafe();

    private StringAdapter() {
    }

    public static JsonAdapter<String> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, String str) throws IOException {
        writer.writeString(str);
    }

    @Override
    public String readObject(JsonReader jsonReader, JavaType<? extends String> type) throws IOException {
        return jsonReader.readString();
    }

}
