package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathAdapter implements JsonAdapter<Path> {

  private static final JsonAdapter<Path> INSTANCE = new PathAdapter().nullSafe();

  private PathAdapter() {}

  public static JsonAdapter<Path> getInstance() {
    return INSTANCE;
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, Path path) throws IOException {
    jsonWriter.writeString(path.toAbsolutePath().toString());
  }

  @Override
  public Path readObject(JsonReader jsonReader, JavaType<? extends Path> type) throws IOException {
    return Paths.get(jsonReader.readString());
  }

}
