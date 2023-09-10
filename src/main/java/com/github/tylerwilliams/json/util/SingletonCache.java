package com.github.tylerwilliams.json.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class SingletonCache {

    private static Map<Class<?>, Object> CACHE = new HashMap<>();

    public static <T> T getInstance(Class<T> klass) {
        return klass.cast(
            CACHE.computeIfAbsent(klass, SingletonCache::createInstance)
        );
    }

    private static <T> T createInstance(Class<T> klass) {
        try {
            Constructor<T> defaultConstructor = klass.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
