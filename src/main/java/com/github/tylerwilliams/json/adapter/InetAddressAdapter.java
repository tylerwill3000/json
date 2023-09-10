package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;
import java.net.InetAddress;

public class InetAddressAdapter implements JsonAdapter<InetAddress> {

    private static final JsonAdapter<InetAddress> INSTANCE = new InetAddressAdapter().nullSafe();

    private InetAddressAdapter() {
    }

    public static JsonAdapter<InetAddress> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, InetAddress inetAddress) throws IOException {
        writer.writeString(inetAddress.toString());
    }

    @Override
    public InetAddress readObject(JsonReader jsonReader, JavaType<? extends InetAddress> type) throws IOException {
        return InetAddress.getByName(jsonReader.readString());
    }

}
