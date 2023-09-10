package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;
import java.util.Currency;

public class CurrencyAdapter implements JsonAdapter<Currency> {

    private static final JsonAdapter<Currency> INSTANCE = new CurrencyAdapter().nullSafe();

    private CurrencyAdapter() {
    }

    public static JsonAdapter<Currency> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, Currency currency) throws IOException {
        writer.writeString(currency.getCurrencyCode());
    }

    @Override
    public Currency readObject(JsonReader reader, JavaType<? extends Currency> type) throws IOException {
        return Currency.getInstance(reader.readString());
    }

}
