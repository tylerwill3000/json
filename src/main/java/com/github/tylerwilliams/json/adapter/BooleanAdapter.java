package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;

public class BooleanAdapter implements JsonAdapter<Boolean> {

    private static final JsonAdapter<Boolean> INSTANCE = new BooleanAdapter().nullSafe();

    private BooleanAdapter() {
    }

    public static JsonAdapter<Boolean> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, Boolean bool) throws IOException {
        writer.writeBoolean(bool);
    }

    @Override
    public Boolean readObject(JsonReader jsonReader, JavaType<? extends Boolean> type) throws IOException {
        return jsonReader.readBoolean();
    }

}
