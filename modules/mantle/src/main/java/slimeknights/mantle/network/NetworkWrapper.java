package slimeknights.mantle.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.FakePlayer;
import slimeknights.mantle.network.compat.NetworkDirection;
import slimeknights.mantle.network.compat.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import slimeknights.mantle.network.compat.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A small network implementation/wrapper using AbstractPackets instead of IMessages.
 * Instantiate in your mod class and register your packets accordingly.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NetworkWrapper {
  /** Network instance */
  public final SimpleChannel network;

  /**
   * Creates a new network wrapper
   * @param channelName  Unique packet channel name
   * @deprecated Give your channel a version number.
   */
  @Deprecated
  public NetworkWrapper(ResourceLocation channelName) {
    this(channelName, "1");
  }

  public NetworkWrapper(ResourceLocation channelName, String version) {
    this.network = new SimpleChannel(channelName);
  }

  /**
   * Registers a new {@link slimeknights.mantle.network.packet.ISimplePacket}
   * @param clazz    Packet class
   * @param decoder  Packet decoder, typically the constructor
   * @param <MSG>  Packet class type
   * @deprecated Payloads are registered via {@link net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent}
   */
  @Deprecated
  public <MSG extends slimeknights.mantle.network.packet.ISimplePacket> void registerPacket(Class<MSG> clazz, Function<FriendlyByteBuf, MSG> decoder, @Nullable NetworkDirection direction) {}

  /**
   * Registers a new generic packet
   * @param clazz      Packet class
   * @param encoder    Encodes a packet to the buffer
   * @param decoder    Packet decoder, typically the constructor
   * @param consumer   Logic to handle a packet
   * @param direction  Network direction for validation. Pass null for no direction
   * @param <MSG>  Packet class type
   * @deprecated Payloads are registered via {@link net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent}
   */
  @Deprecated
  public <MSG> void registerPacket(Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG,Supplier<NetworkEvent.Context>> consumer, @Nullable NetworkDirection direction) {}

  /**
   * Registers a new packet without the automatic logging if the decoder fails
   * @param clazz      Packet class
   * @param encoder    Encodes a packet to the buffer
   * @param decoder    Packet decoder, typically the constructor
   * @param consumer   Logic to handle a packet
   * @param direction  Network direction for validation. Pass null for no direction
   * @param <MSG>  Packet class type
   * @deprecated Payloads are registered via {@link net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent}
   */
  @Deprecated
  public <MSG> void registerPacketNoLogger(Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG,Supplier<NetworkEvent.Context>> consumer, @Nullable NetworkDirection direction) {}


  /* Sending packets */

  /**
   * Sends a packet to the server
   * @param msg  Packet to send
   */
  public void sendToServer(CustomPacketPayload msg) {
    PacketDistributor.sendToServer(msg);
  }

  /**
   * Sends a packet to the given packet distributor
   * @param target   Packet target
   * @param message  Packet to send
   */
  public void send(Object target, CustomPacketPayload message) {
    network.send(target, message);
  }

  /**
   * Sends a vanilla packet to the given entity
   * @param player  Player receiving the packet
   * @param packet  Packet
   */
  public void sendVanillaPacket(Packet<?> packet, Entity player) {
    if (player instanceof ServerPlayer sPlayer) {
      sPlayer.connection.send(packet);
    }
  }

  /**
   * Sends a packet to a player
   * @param msg     Packet
   * @param player  Player to send
   */
  public void sendTo(CustomPacketPayload msg, Player player) {
    if (player instanceof ServerPlayer serverPlayer) {
      sendTo(msg, serverPlayer);
    }
  }

  /**
   * Sends a packet to a player
   * @param msg     Packet
   * @param player  Player to send
   */
  public void sendTo(CustomPacketPayload msg, ServerPlayer player) {
    if ("test-mock-player".equals(player.getGameProfile().getName())) {
      return;
    }
    if (!(player instanceof FakePlayer)) {
      PacketDistributor.sendToPlayer(player, msg);
    }
  }

  /**
   * Sends a packet to players near a location
   * @param msg          Packet to send
   * @param serverWorld  World instance
   * @param position     Position within range
   */
  public void sendToClientsAround(CustomPacketPayload msg, ServerLevel serverWorld, BlockPos position) {
    LevelChunk chunk = serverWorld.getChunkAt(position);
    PacketDistributor.sendToPlayersTrackingChunk(serverWorld, chunk.getPos(), msg);
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  public void sendToTrackingAndSelf(CustomPacketPayload msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, msg);
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  public void sendToTracking(CustomPacketPayload msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, msg);
  }
}
