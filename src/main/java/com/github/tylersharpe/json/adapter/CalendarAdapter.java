package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Adapter which reads and writes {@link Calendar}s. The adapter will delegate to the
 * underlying {@link DateAdapter} in use for the current reader / writer
 */
public class CalendarAdapter implements JsonAdapter<Calendar> {

    private static final JsonAdapter<Calendar> INSTANCE = new CalendarAdapter().nullSafe();

    private CalendarAdapter() {
    }

    public static JsonAdapter<Calendar> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, Calendar calendar) throws IOException {
        writer.writeValue(calendar.getTime());
    }

    @Override
    public Calendar readObject(JsonReader reader, JavaType<? extends Calendar> type) throws IOException {
        Date date = reader.readClass(Date.class);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

}
