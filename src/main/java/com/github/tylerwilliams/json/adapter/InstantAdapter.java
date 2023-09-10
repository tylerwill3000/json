package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;
import java.time.Instant;

public class InstantAdapter implements JsonAdapter<Instant> {

    private static final JsonAdapter<Instant> INSTANCE = new InstantAdapter().nullSafe();

    private InstantAdapter() {
    }

    public static JsonAdapter<Instant> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, Instant instant) throws IOException {
        writer.writeLong(instant.toEpochMilli());
    }

    @Override
    public Instant readObject(JsonReader reader, JavaType<? extends Instant> type) throws IOException {
        return Instant.ofEpochMilli(reader.readLong());
    }

}
