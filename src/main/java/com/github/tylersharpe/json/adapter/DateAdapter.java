package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonBindException;
import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class DateAdapter implements JsonAdapter<Date> {

  private String dateFormat;

  DateAdapter(String dateFormat) {
    this.dateFormat = Objects.requireNonNull(dateFormat, "Date format string may not be null");
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, Date date) throws IOException {
    jsonWriter.writeValue(new SimpleDateFormat(dateFormat).format(date));
  }

  @Override
  public Date readObject(JsonReader reader, JavaType<? extends Date> type) throws IOException {
    try {
      return new SimpleDateFormat(dateFormat).parse(reader.readString());
    } catch (ParseException e) {
      throw new JsonBindException(e);
    }
  }

}
