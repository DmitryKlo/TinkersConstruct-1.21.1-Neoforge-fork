package slimeknights.mantle.registration;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Small compatibility wrapper around NeoForge's {@link DeferredHolder}.
 */
public final class RegistryObject<T> implements Supplier<T> {
  private final DeferredHolder<?, ?> holder;

  public RegistryObject(DeferredHolder<?, ?> holder) {
    this.holder = holder;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T get() {
    return (T) holder.get();
  }

  @SuppressWarnings("unchecked")
  public Optional<T> asOptional() {
    return holder.asOptional().map(value -> (T) value);
  }

  public boolean isPresent() {
    return holder.isBound();
  }

  public ResourceLocation getId() {
    return holder.getId();
  }

  @SuppressWarnings("unchecked")
  public ResourceKey<T> getKey() {
    return (ResourceKey<T>) holder.getKey();
  }
}
