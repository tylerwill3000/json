package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.util.UUID;

public class UUIDAdapter implements JsonAdapter<UUID> {

  private static final JsonAdapter<UUID> INSTANCE = new UUIDAdapter().nullSafe();

  private UUIDAdapter() {}

  public static JsonAdapter<UUID> getInstance() {
    return INSTANCE;
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, UUID uuid) throws IOException {
    jsonWriter.writeString(uuid.toString());
  }

  @Override
  public UUID readObject(JsonReader jsonReader, JavaType<? extends UUID> type) throws IOException {
    return UUID.fromString(jsonReader.readString());
  }

}
