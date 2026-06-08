package slimeknights.mantle.data.registry;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic registry of a component named by a resource location. Supports any arbitrary object without making any changes to it.
 * @param <T> Type of the component being registered.
 */
public class NamedComponentRegistry<T> extends AbstractNamedComponentRegistry<T> {
  /** Registered box expansion types */
  private final Map<ResourceLocation,T> values = new LinkedHashMap<>();
  /** First registered name for each value, used for serialization when several aliases share a loader. */
  private final Map<T,ResourceLocation> keysByValue = new IdentityHashMap<>();
  public NamedComponentRegistry(String errorText) {
    super(errorText);
  }

  /** Registers the value with the given name */
  public synchronized <V extends T> V register(ResourceLocation name, V value) {
    if (values.putIfAbsent(name, value) != null) {
      throw new IllegalArgumentException("Duplicate registration " + name);
    }
    keysByValue.putIfAbsent(value, name);
    return value;
  }

  @Override
  @Nullable
  public T getValue(ResourceLocation name) {
    return values.get(name);
  }

  /** Gets the key associated with a value */
  @Nullable
  public ResourceLocation getOptionalKey(T value) {
    return keysByValue.get(value);
  }

  @Override
  public ResourceLocation getKey(T value) {
    ResourceLocation key = getOptionalKey(value);
    if (key == null) {
      throw new IllegalStateException(errorText + value);
    }
    return key;
  }

  @Override
  public Collection<ResourceLocation> getKeys() {
    return values.keySet();
  }

  @Override
  public Collection<T> getValues() {
    return values.values();
  }
}
