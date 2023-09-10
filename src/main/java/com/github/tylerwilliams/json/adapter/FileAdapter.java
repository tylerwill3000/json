package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.File;
import java.io.IOException;

public class FileAdapter implements JsonAdapter<File> {

    private static final JsonAdapter<File> INSTANCE = new FileAdapter().nullSafe();

    private FileAdapter() {
    }

    public static JsonAdapter<File> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, File file) throws IOException {
        writer.writeString(file.getAbsolutePath());
    }

    @Override
    public File readObject(JsonReader jsonReader, JavaType<? extends File> type) throws IOException {
        return new File(jsonReader.readString());
    }

}
