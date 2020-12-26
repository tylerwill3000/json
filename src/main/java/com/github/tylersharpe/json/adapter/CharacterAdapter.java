package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;

public class CharacterAdapter implements JsonAdapter<Character> {

    private static final JsonAdapter<Character> INSTANCE = new CharacterAdapter().nullSafe();

    private CharacterAdapter() {
    }

    public static JsonAdapter<Character> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, Character c) throws IOException {
        writer.writeString(Character.toString(c));
    }

    @Override
    public Character readObject(JsonReader jsonReader, JavaType<? extends Character> type) throws IOException {
        return jsonReader.readString().charAt(0);
    }

}
