package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;

public class StringAdapter implements JsonAdapter<String> {

    private static final JsonAdapter<String> INSTANCE = new StringAdapter().nullSafe();

    private StringAdapter() {
    }

    public static JsonAdapter<String> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter jsonWriter, String str) throws IOException {
        jsonWriter.writeString(str);
    }

    @Override
    public String readObject(JsonReader jsonReader, JavaType<? extends String> type) throws IOException {
        return jsonReader.readString();
    }

}
