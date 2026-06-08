package slimeknights.mantle.network.compat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import slimeknights.mantle.network.compat.NetworkDirection;
import slimeknights.mantle.network.compat.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleChannel {
  private final ResourceLocation name;

  public SimpleChannel(ResourceLocation name) {
    this.name = name;
  }

  public <MSG> void registerMessage(int id, Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer, Optional<NetworkDirection> direction) {}

  public void sendToServer(CustomPacketPayload msg) {
    PacketDistributor.sendToServer(msg);
  }

  public void send(Object target, CustomPacketPayload msg) {}

  public void sendTo(CustomPacketPayload msg, ServerPlayer player, NetworkDirection direction) {
    PacketDistributor.sendToPlayer(player, msg);
  }

  public ResourceLocation name() {
    return name;
  }
}
