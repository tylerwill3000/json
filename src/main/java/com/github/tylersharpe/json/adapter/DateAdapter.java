package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.sql.Timestamp;
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
    jsonWriter.writeString(new SimpleDateFormat(dateFormat).format(date));
  }

  @Override
  public Date readObject(JsonReader reader, JavaType<? extends Date> type) throws IOException {
    Class<? extends Date> dateClass = type.getRawType();

    String dateString = reader.readString();
    try {
      Date date = new SimpleDateFormat(dateFormat).parse(dateString);

      if (dateClass == Date.class) {
        return date;
      } else if (dateClass == Timestamp.class) {
        return new Timestamp(date.getTime());
      } else {
        throw reader.createJsonBindException("Cannot deserialize to unknown date subclass " + dateClass.getName());
      }
    } catch (ParseException e) {
      throw reader.createJsonBindException("Could not parse '" + dateString + "' as " + dateClass, e);
    }
  }

}
