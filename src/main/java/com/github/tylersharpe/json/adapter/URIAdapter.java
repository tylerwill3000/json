package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonBindException;
import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class URIAdapter implements JsonAdapter<URI> {

  private static final JsonAdapter<URI> INSTANCE = new URIAdapter().nullSafe();

  private URIAdapter() {}

  public static JsonAdapter<URI> getInstance() {
    return INSTANCE;
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, URI uri) throws IOException {
    jsonWriter.writeString(uri.toString());
  }

  @Override
  public URI readObject(JsonReader jsonReader, JavaType<? extends URI> type) throws IOException {
    try {
      return new URI(jsonReader.readString());
    } catch (URISyntaxException e) {
      throw new JsonBindException(e);
    }
  }

}
