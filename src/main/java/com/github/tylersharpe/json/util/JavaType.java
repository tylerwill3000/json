package com.github.tylersharpe.json.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@SuppressWarnings({"unchecked", "rawtypes"})
public class JavaType<C> {

    private static final Map<Type, JavaType<?>> CACHE = new HashMap<>();

    private Class<C> rawType;
    private List<Type> genericTypes;

    private JavaType(Class<C> rawType, List<Type> genericTypes) {
        this.rawType = rawType;
        this.genericTypes = genericTypes;
    }

    public static <C> JavaType<C> from(Type type) {
        return (JavaType<C>) CACHE.computeIfAbsent(type, JavaType::create);
    }

    private static JavaType create(Type type) {
        if (type instanceof Class classType) {
            return new JavaType(classType, List.of());

        } else if (type instanceof ParameterizedType paramType) {
            var rawType = (Class) paramType.getRawType();
            var genericTypes = Stream.of(paramType.getActualTypeArguments()).collect(toList());
            return new JavaType(rawType, genericTypes);

        } else if (type instanceof GenericArrayType arrayType) {
            Type componentType = arrayType.getGenericComponentType();
            return create(componentType);

        } else {
            throw new IllegalArgumentException("Cannot determine raw class from " + type);
        }
    }

    public Class<C> getRawType() {
        return rawType;
    }

    public Optional<Type> getGenericType(int index) {
        if (genericTypes == null || index >= genericTypes.size()) {
            return Optional.empty();
        }
        return Optional.of(genericTypes.get(index));
    }

    /**
     * Checks the upper / lower bounds of the given wildcard type to see if there is a more
     * specific bound type than Object. If not, just returns Object.
     */
    public static Type parseBoundType(WildcardType wildcardType) {
        Type upperBoundType = wildcardType.getUpperBounds()[0];
        if (upperBoundType != Object.class) {
            return upperBoundType;
        }
        return wildcardType.getLowerBounds()[0];
    }

}
