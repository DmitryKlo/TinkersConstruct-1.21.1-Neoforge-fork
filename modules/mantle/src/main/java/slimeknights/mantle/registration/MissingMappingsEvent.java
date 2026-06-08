package slimeknights.mantle.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

import java.util.List;

public class MissingMappingsEvent extends Event {
  public <T> List<Mapping<T>> getAllMappings(ResourceKey<? extends Registry<T>> registry) {
    return List.of();
  }

  public static class Mapping<T> {
    private final ResourceLocation key;

    public Mapping(ResourceLocation key) {
      this.key = key;
    }

    public ResourceLocation getKey() {
      return key;
    }

    public void remap(T value) {}
  }
}
