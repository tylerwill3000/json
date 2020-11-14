package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;

public class BooleanAdapter implements JsonAdapter<Boolean> {

    private static final JsonAdapter<Boolean> INSTANCE = new BooleanAdapter().nullSafe();

    private BooleanAdapter() {
    }

    public static JsonAdapter<Boolean> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter jsonWriter, Boolean bool) throws IOException {
        jsonWriter.writeBoolean(bool);
    }

    @Override
    public Boolean readObject(JsonReader jsonReader, JavaType<? extends Boolean> type) throws IOException {
        return jsonReader.readBoolean();
    }

}
