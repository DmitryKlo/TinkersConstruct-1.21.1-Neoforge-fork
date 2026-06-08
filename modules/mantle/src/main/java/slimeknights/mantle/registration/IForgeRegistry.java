package slimeknights.mantle.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Compatibility bridge for Mantle's Forge-era registry helpers.
 *
 * <p>NeoForge 1.21 uses vanilla {@link Registry} plus {@link DeferredHolder};
 * this interface keeps the old helper APIs compiling while those helpers are
 * migrated subsystem by subsystem.</p>
 */
public interface IForgeRegistry<T> {
  Registry<T> unwrap();

  ResourceKey<? extends Registry<T>> registryKey();

  @Nullable
  default ResourceLocation getKey(T value) {
    return unwrap().getKey(value);
  }

  @Nullable
  default T getValue(ResourceLocation location) {
    return unwrap().get(location);
  }

  default boolean containsKey(ResourceLocation location) {
    return unwrap().containsKey(location);
  }

  default ResourceLocation getRegistryName() {
    return registryKey().location();
  }

  default <I extends T> I register(ResourceLocation location, I value) {
    Registry.register(unwrap(), location, value);
    return value;
  }

  default IForgeRegistry<T> get() {
    return this;
  }
}
