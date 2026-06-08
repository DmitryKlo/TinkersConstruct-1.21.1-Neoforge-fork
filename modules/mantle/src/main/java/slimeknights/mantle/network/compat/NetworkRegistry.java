package slimeknights.mantle.network.compat;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.network.compat.SimpleChannel;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NetworkRegistry {
  private NetworkRegistry() {}

  public static final class ChannelBuilder {
    private ResourceLocation name;

    private ChannelBuilder(ResourceLocation name) {
      this.name = name;
    }

    public static ChannelBuilder named(ResourceLocation name) {
      return new ChannelBuilder(name);
    }

    public ChannelBuilder clientAcceptedVersions(Predicate<String> predicate) {
      return this;
    }

    public ChannelBuilder serverAcceptedVersions(Predicate<String> predicate) {
      return this;
    }

    public ChannelBuilder networkProtocolVersion(Supplier<String> version) {
      return this;
    }

    public SimpleChannel simpleChannel() {
      return new SimpleChannel(name);
    }
  }
}
