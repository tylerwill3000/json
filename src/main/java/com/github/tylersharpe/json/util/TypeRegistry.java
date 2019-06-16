package com.github.tylersharpe.json.util;

import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

/**
 * Registry which associate values with java class types.
 * <br/><br/>
 * Unlike a simple {@link HashMap}, this registry is intelligent in that if no direct mapping is present for a given
 * class, it will return the mapped value from the nearest superclass or superinterface to that class (if present).
 * This allows to create high-level, generic mappings which can be applied to wide classes from objects
 *
 * @param <V> The type from values associated to classes in this registry
 */
public class TypeRegistry<V> {

  private Map<Class<?>, V> registry = new HashMap<>();
  private Set<Class<?>> mappedSubclasses = new HashSet<>();

  public TypeRegistry() {}

  public TypeRegistry(TypeRegistry<V> other) {
    registry.putAll(other.registry);
    mappedSubclasses.addAll(other.mappedSubclasses);
  }

  public TypeRegistry<V> register(final Class<?> clazz, V value) {
    registry.put(clazz, value);

    Collection<Class<?>> subclassesToRemove = mappedSubclasses.stream().filter(sc -> clazz.isAssignableFrom(sc) && clazz != sc).collect(toList());
    for (Class<?> subclassToRemove : subclassesToRemove) {
      mappedSubclasses.remove(subclassToRemove);
      registry.remove(subclassToRemove);
    }

    return this;
  }

  public V get(final Class<?> clazz) {
    return registry.computeIfAbsent(clazz, this::getForNearestSuperclass);
  }

  private V getForNearestSuperclass(Class<?> clazz) {
    mappedSubclasses.add(clazz);

    if (clazz.isArray()) {
      return registry.get(Object[].class);
    }

    TreeMap<Integer, List<Class<?>>> superClassesByDistance = registry.keySet()
            .stream()
            .filter(mappedClass -> mappedClass.isAssignableFrom(clazz))
            .collect(groupingBy(superClass -> classDistance(clazz, superClass), TreeMap::new, toList()));

    if (!superClassesByDistance.isEmpty()) {
      List<Class<?>> nearestSuperclasses = superClassesByDistance.entrySet().iterator().next().getValue();
      nearestSuperclasses.sort(comparing(Class::isInterface));
      // Object will be ordered before interfaces since Object is a class, but we want interfaces to take precedence over Object
      Class<?> superclass = nearestSuperclasses.get(0).equals(Object.class) && nearestSuperclasses.size() > 1 ? nearestSuperclasses.get(1) : nearestSuperclasses.get(0);
      return registry.get(superclass);
    } else {
      return null;
    }
  }

  static int classDistance(Class<?> subClass, Class<?> superClass) {
    if (subClass == superClass) {
      return 0;
    }

    if (!superClass.isAssignableFrom(subClass)) {
      throw new IllegalArgumentException(subClass + " is not a subclass from " + superClass);
    }

    int distance = 0;
    Queue<Set<Class<?>>> searchQueue = new ArrayDeque<>();
    searchQueue.add(getSuperAndInterfaces(subClass));

    while (!searchQueue.isEmpty()) {
      Set<Class<?>> currentLevel = searchQueue.poll();
      distance++;

      if (currentLevel.contains(superClass)) {
        break;
      }

      Set<Class<?>> nextLevel = currentLevel.stream()
              .flatMap(clazz -> getSuperAndInterfaces(clazz).stream())
              .collect(toSet());

      if (!nextLevel.isEmpty()) {
        searchQueue.add(nextLevel);
      }
    }

    return distance;
  }

  private static Set<Class<?>> getSuperAndInterfaces(Class<?> clazz) {
    Set<Class<?>> superAndInterfaces = new HashSet<>();
    if (clazz.getSuperclass() != null) {
      superAndInterfaces.add(clazz.getSuperclass());
    }
    Collections.addAll(superAndInterfaces, clazz.getInterfaces());
    return superAndInterfaces;
  }

  @Override
  public String toString() {
    return registry.toString();
  }

}
