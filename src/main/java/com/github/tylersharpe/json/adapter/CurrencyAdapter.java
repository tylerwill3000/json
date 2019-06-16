package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.util.Currency;

public class CurrencyAdapter implements JsonAdapter<Currency> {

  private static final JsonAdapter<Currency> INSTANCE = new CurrencyAdapter().nullSafe();

  private CurrencyAdapter() {}

  public static JsonAdapter<Currency> getInstance() {
    return INSTANCE;
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, Currency currency) throws IOException {
    jsonWriter.writeValue(currency.getCurrencyCode());
  }

  @Override
  public Currency readObject(JsonReader reader, JavaType<? extends Currency> type) throws IOException {
    return Currency.getInstance(reader.readString());
  }

}
