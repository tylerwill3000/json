package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.util.TypeRegistry;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Creates and registers all built-in {@link JsonAdapter}s for common types
 */
public class DefaultAdapterHolder {

    private static final TypeRegistry<JsonAdapter<?>> DEFAULT_ADAPTERS = new TypeRegistry<>();
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    static {
        DEFAULT_ADAPTERS.register(byte.class, NumberAdapter.getInstance());
        DEFAULT_ADAPTERS.register(short.class, NumberAdapter.getInstance());
        DEFAULT_ADAPTERS.register(int.class, NumberAdapter.getInstance());
        DEFAULT_ADAPTERS.register(long.class, NumberAdapter.getInstance());
        DEFAULT_ADAPTERS.register(float.class, NumberAdapter.getInstance());
        DEFAULT_ADAPTERS.register(double.class, NumberAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Number.class, NumberAdapter.getInstance());
        DEFAULT_ADAPTERS.register(boolean.class, BooleanAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Boolean.class, BooleanAdapter.getInstance());
        DEFAULT_ADAPTERS.register(char.class, CharacterAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Character.class, CharacterAdapter.getInstance());
        DEFAULT_ADAPTERS.register(String.class, StringAdapter.getInstance());
        DEFAULT_ADAPTERS.register(UUID.class, UUIDAdapter.getInstance());
        DEFAULT_ADAPTERS.register(URL.class, URLAdapter.getInstance());
        DEFAULT_ADAPTERS.register(URI.class, URIAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Path.class, PathAdapter.getInstance());
        DEFAULT_ADAPTERS.register(File.class, FileAdapter.getInstance());
        DEFAULT_ADAPTERS.register(InetAddress.class, InetAddressAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Year.class, new TemporalAdapter<>(DateTimeFormatter.ofPattern("yyyy")).nullSafe());
        DEFAULT_ADAPTERS.register(YearMonth.class, new TemporalAdapter<>(DateTimeFormatter.ofPattern("yyyy-MM")).nullSafe());
        DEFAULT_ADAPTERS.register(MonthDay.class, new TemporalAdapter<>(DateTimeFormatter.ofPattern("MM-dd")).nullSafe());
        DEFAULT_ADAPTERS.register(LocalDate.class, new TemporalAdapter<>(DateTimeFormatter.ISO_LOCAL_DATE).nullSafe());
        DEFAULT_ADAPTERS.register(LocalTime.class, new TemporalAdapter<>(DateTimeFormatter.ISO_LOCAL_TIME).nullSafe());
        DEFAULT_ADAPTERS.register(OffsetTime.class, new TemporalAdapter<>(DateTimeFormatter.ISO_TIME).nullSafe());
        DEFAULT_ADAPTERS.register(LocalDateTime.class, new TemporalAdapter<>(DateTimeFormatter.ISO_LOCAL_DATE_TIME).nullSafe());
        DEFAULT_ADAPTERS.register(ZonedDateTime.class, new TemporalAdapter<>(DateTimeFormatter.ISO_DATE_TIME).nullSafe());
        DEFAULT_ADAPTERS.register(OffsetDateTime.class, new TemporalAdapter<>(DateTimeFormatter.ISO_OFFSET_DATE_TIME).nullSafe());
        DEFAULT_ADAPTERS.register(Instant.class, InstantAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Date.class, new DateAdapter(DEFAULT_DATE_FORMAT).nullSafe());
        DEFAULT_ADAPTERS.register(Calendar.class, CalendarAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Locale.class, LocaleAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Currency.class, CurrencyAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Enum.class, EnumAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Collection.class, CollectionAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Object[].class, ArrayAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Map.class, MapAdapter.getInstance());
        DEFAULT_ADAPTERS.register(Object.class, ObjectAdapter.getInstance());
    }

    public static TypeRegistry<JsonAdapter<?>> newRegistry() {
        return new TypeRegistry<>(DEFAULT_ADAPTERS);
    }

}
