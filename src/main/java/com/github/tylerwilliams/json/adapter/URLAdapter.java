package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class URLAdapter implements JsonAdapter<URL> {

    private static final JsonAdapter<URL> INSTANCE = new URLAdapter().nullSafe();

    private URLAdapter() {
    }

    public static JsonAdapter<URL> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, URL url) throws IOException {
        writer.writeString(url.toString());
    }

    @Override
    public URL readObject(JsonReader jsonReader, JavaType<? extends URL> type) throws IOException {
        String nextString = jsonReader.readString();
        try {
            return new URL(nextString);
        } catch (MalformedURLException e) {
            throw jsonReader.createJsonBindException("Could not parse URL from '" + nextString + "'", e);
        }
    }

}
