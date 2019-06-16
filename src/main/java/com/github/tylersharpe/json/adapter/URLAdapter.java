package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.net.URL;

public class URLAdapter implements JsonAdapter<URL> {

  private static final JsonAdapter<URL> INSTANCE = new URLAdapter().nullSafe();

  private URLAdapter() {}

  public static JsonAdapter<URL> getInstance() {
    return INSTANCE;
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, URL url) throws IOException {
    jsonWriter.writeString(url.toString());
  }

  @Override
  public URL readObject(JsonReader jsonReader, JavaType<? extends URL> type) throws IOException {
    return new URL(jsonReader.readString());
  }

}
