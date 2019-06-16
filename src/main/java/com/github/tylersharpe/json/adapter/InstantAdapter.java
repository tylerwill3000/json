package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.time.Instant;

public class InstantAdapter implements JsonAdapter<Instant> {

  private static final JsonAdapter<Instant> INSTANCE = new InstantAdapter().nullSafe();

  private InstantAdapter() {}

  public static JsonAdapter<Instant> getInstance() {
    return INSTANCE;
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, Instant instant) throws IOException {
    jsonWriter.writeValue(instant.toEpochMilli());
  }

  @Override
  public Instant readObject(JsonReader reader, JavaType<? extends Instant> type) throws IOException {
    return Instant.ofEpochMilli(reader.readLong());
  }

}
