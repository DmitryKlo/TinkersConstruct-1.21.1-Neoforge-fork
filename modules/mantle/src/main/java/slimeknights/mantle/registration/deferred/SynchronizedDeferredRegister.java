package slimeknights.mantle.registration.deferred;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.registration.IForgeRegistry;
import slimeknights.mantle.registration.RegistryObject;

import java.util.function.Supplier;

/** Deferred register instance that synchronizes register calls */
@RequiredArgsConstructor(staticName = "create")
public class SynchronizedDeferredRegister<T> {
  private final DeferredRegister<T> internal;

  /** Creates a new instance for the given resource key */
  public static <T> SynchronizedDeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modid) {
    return create(DeferredRegister.create(key, modid));
  }

  /** Creates a new instance for the given forge registry */
  public static <B> SynchronizedDeferredRegister<B> create(IForgeRegistry<B> registry, String modid) {
    return create(DeferredRegister.create(registry.registryKey(), modid));
  }

  /** Registers the given object, synchronized over the internal register */
  public <I extends T> RegistryObject<I> register(final String name, final Supplier<? extends I> sup) {
    synchronized (internal) {
      DeferredHolder<T, I> holder = internal.register(name, sup);
      return new RegistryObject<>(holder);
    }
  }

  /**
   * Registers the internal register with the event bus
   */
  public void register(IEventBus bus) {
    internal.register(bus);
  }

  /** Registers a legacy alias for registry remapping on world load */
  public void addAlias(ResourceLocation oldName, ResourceLocation newName) {
    internal.addAlias(oldName, newName);
  }
}
