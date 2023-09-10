package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;
import java.util.UUID;

public class UUIDAdapter implements JsonAdapter<UUID> {

    private static final JsonAdapter<UUID> INSTANCE = new UUIDAdapter().nullSafe();

    private UUIDAdapter() {
    }

    public static JsonAdapter<UUID> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, UUID uuid) throws IOException {
        writer.writeString(uuid.toString());
    }

    @Override
    public UUID readObject(JsonReader jsonReader, JavaType<? extends UUID> type) throws IOException {
        String nextString = jsonReader.readString();
        try {
            return UUID.fromString(nextString);
        } catch (IllegalArgumentException e) {
            throw jsonReader.createJsonBindException("Could not parse UUID from '" + nextString + "'", e);
        }
    }

}
