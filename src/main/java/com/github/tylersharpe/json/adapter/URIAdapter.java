package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class URIAdapter implements JsonAdapter<URI> {

    private static final JsonAdapter<URI> INSTANCE = new URIAdapter().nullSafe();

    private URIAdapter() {
    }

    public static JsonAdapter<URI> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, URI uri) throws IOException {
        writer.writeString(uri.toString());
    }

    @Override
    public URI readObject(JsonReader jsonReader, JavaType<? extends URI> type) throws IOException {
        String nextString = jsonReader.readString();
        try {
            return new URI(nextString);
        } catch (URISyntaxException e) {
            throw jsonReader.createJsonBindException("Could not parse URI from '" + nextString + "'", e);
        }
    }

}
