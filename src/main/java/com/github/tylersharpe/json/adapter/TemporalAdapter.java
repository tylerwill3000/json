package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

public class TemporalAdapter<T extends TemporalAccessor> implements JsonAdapter<T> {

  private final DateTimeFormatter formatter;

  TemporalAdapter(DateTimeFormatter formatter) {
    this.formatter = Objects.requireNonNull(formatter, "formatter parameter cannot be null");
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, T temporal) throws IOException {
    jsonWriter.writeString(formatter.format(temporal));
  }

  @Override
  public T readObject(JsonReader reader, JavaType<? extends T> temporalType) throws IOException {
    TemporalAccessor temporal = formatter.parse(reader.readString());
    return temporalType.getRawType().cast(temporal);
  }

}
