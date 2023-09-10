package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;
import java.util.Locale;

public class LocaleAdapter implements JsonAdapter<Locale> {

    private static final JsonAdapter<Locale> INSTANCE = new LocaleAdapter().nullSafe();

    private LocaleAdapter() {
    }

    public static JsonAdapter<Locale> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, Locale locale) throws IOException {
        writer.writeString(locale.toLanguageTag());
    }

    @Override
    public Locale readObject(JsonReader reader, JavaType<? extends Locale> type) throws IOException {
        return Locale.forLanguageTag(reader.readString());
    }

}
