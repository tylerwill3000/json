package com.github.tylersharpe.json;

import com.github.tylersharpe.json.adapter.DefaultAdapterHolder;
import com.github.tylersharpe.json.adapter.JsonAdapter;
import com.github.tylersharpe.json.util.TypeRegistry;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Central entry point to the JSON serialization API.
 * Provides various methods for serializing and parsing JSON objects
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Json implements SerializationContext {

  private WhitespaceWriter whitespaceWriter;
  private boolean serializeNulls;
  private TypeRegistry<JsonAdapter<?>> adapters;

  public Json() {
    this.whitespaceWriter = WhitespaceWriter.NO_WHITESPACE;
    this.serializeNulls = false;
    this.adapters = DefaultAdapterHolder.newRegistry();
  }

  public void setWhitespaceWriter(WhitespaceWriter whitespaceWriter) {
    this.whitespaceWriter = whitespaceWriter;
  }

  @Override
  public WhitespaceWriter getWhitespaceWriter() {
    return this.whitespaceWriter;
  }

  public void setSerializeNulls(boolean serializeNulls) {
    this.serializeNulls = serializeNulls;
  }

  @Override
  public boolean isSerializeNulls() {
    return this.serializeNulls;
  }

  public <T> void registerAdapter(Class<T> clazz, JsonAdapter<? super T> adapter) {
    adapters.register(clazz, adapter);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> JsonAdapter<? super T> getAdapter(Class<T> klass) {
    return (JsonAdapter<? super T>) this.adapters.get(klass);
  }

  public Object parse(String str) throws IOException {
    return parse(str, Object.class);
  }

  public <T> T parse(String str, Class<T> bindClass) throws IOException {
    Object result = parse(str, (Type) bindClass);
    return bindClass.cast(result);
  }

  public Object parse(String str, Type bindType) throws IOException {
    var inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    return parse(inputStream, bindType);
  }

  public Object parse(URL url) throws IOException {
    return parse(url, Object.class);
  }

  public <T> T parse(URL url, Class<T> bindClass) throws IOException {
    Object result = parse(url, (Type) bindClass);
    return bindClass.cast(result);
  }

  public Object parse(URL url, Type bindType) throws IOException {
    try (var urlStream = url.openStream()) {
      return parse(urlStream, bindType);
    }
  }

  public Object parse(InputStream input) throws IOException {
    return parse(input, Object.class);
  }

  public <T> T parse(InputStream input, Class<T> bindClass) throws IOException {
    Object result = parse(input, (Type) bindClass);
    return bindClass.cast(result);
  }

  public Object parse(InputStream input, Type bindType) throws IOException {
    var jsonReader = new JsonReader(input, this);
    return jsonReader.readType(bindType);
  }

  public String serialize(Object obj) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serialize(obj, out);
    return new String(out.toByteArray(), StandardCharsets.UTF_8);
  }

  public void serialize(Object obj, OutputStream out) throws IOException {
    var jsonWriter = new JsonWriter(out, this);
    jsonWriter.writeValue(obj);
    jsonWriter.flush();
  }

}
