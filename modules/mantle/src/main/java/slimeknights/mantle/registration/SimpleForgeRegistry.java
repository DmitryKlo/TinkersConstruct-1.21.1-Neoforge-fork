package slimeknights.mantle.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public record SimpleForgeRegistry<T>(Registry<T> unwrap, ResourceKey<? extends Registry<T>> registryKey) implements IForgeRegistry<T> {}
