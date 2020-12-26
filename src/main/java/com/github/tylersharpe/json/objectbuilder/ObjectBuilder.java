package com.github.tylersharpe.json.objectbuilder;

import com.github.tylersharpe.json.JsonBindException;
import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.adapter.JsonAdapter;
import com.github.tylersharpe.json.adapter.TypeMetadataAdapter;
import com.github.tylersharpe.json.annotation.JsonConstructor;
import com.github.tylersharpe.json.annotation.JsonIgnoreExtraFields;
import com.github.tylersharpe.json.annotation.JsonProperty;
import com.github.tylersharpe.json.annotation.JsonSerialization;
import com.github.tylersharpe.json.util.JavaType;
import com.github.tylersharpe.json.util.SingletonCache;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Builds objects field-by-field as entries are visited in a JSON object or array
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public interface ObjectBuilder<T> {

    Map<Class, Supplier<ObjectBuilder>> OBJECT_BUILDER_CACHE = new HashMap<>();

    /**
     * Accumulate a field value from the next object / array entry
     */
    void accumulateField(JsonReader reader) throws IOException;

    /**
     * @return The built object after having visited all entries in the upcoming array / object
     */
    T buildObject();

    static <T> ObjectBuilder<T> newObjectBuilder(Class<T> klass) {
        if (klass.isInterface() || Modifier.isAbstract(klass.getModifiers())) {
            throw new JsonBindException(
                "Cannot directly instantiate " + klass + " since it is an abstract class or an interface. " +
                "If you intend to deserialize instances of this type, consider using " + TypeMetadataAdapter.class.getName()
            );
        }

        Supplier<ObjectBuilder> builderSupplier = OBJECT_BUILDER_CACHE.computeIfAbsent(klass, rawType -> {
            for (var constructor : rawType.getDeclaredConstructors()) {
                if (constructor.isAnnotationPresent(JsonConstructor.class)) {
                    return () -> new ConstructorBuilder(constructor);
                }
            }
            return () -> new FieldBuilder(rawType);
        });

        return (ObjectBuilder<T>) builderSupplier.get();
    }

    static Object readFieldValue(JsonReader reader, Field field) throws IOException {
        if (field.isAnnotationPresent(JsonSerialization.class)) {
            var adapterClass = field.getAnnotation(JsonSerialization.class).value();
            JsonAdapter jsonAdapter = SingletonCache.getInstance(adapterClass);
            return jsonAdapter.readObject(reader, JavaType.from(field.getGenericType()));
        } else {
            return reader.readType(field.getGenericType());
        }
    }

    static Field findFieldForJsonProperty(Class clazz, String jsonProperty) {
        try {
            return clazz.getDeclaredField(jsonProperty);
        } catch (NoSuchFieldException ignore) {
            return Stream.of(clazz.getDeclaredFields())
                .filter(field ->
                    field.isAnnotationPresent(JsonProperty.class) && field.getDeclaredAnnotation(JsonProperty.class).value().equals(jsonProperty)
                )
                .findFirst()
                .orElseGet(() -> {
                    if (clazz.isAnnotationPresent(JsonIgnoreExtraFields.class)) {
                        return null;
                    }
                    throw new JsonBindException("Cannot find field in " + clazz + " to bind json property '" + jsonProperty + "' to");
                });
        }
    }

}
