package slimeknights.mantle.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;

/**
 * Packet interface to add common methods for registration
 */
public interface ISimplePacket extends CustomPacketPayload {
  Type<CustomPacketPayload> LEGACY_TYPE = new Type<>(Mantle.getResource("legacy_packet"));

  @Override
  default Type<? extends CustomPacketPayload> type() {
    return LEGACY_TYPE;
  }

  default void encode(FriendlyByteBuf buffer) {}

  /**
   * Handles receiving the packet
   * @param context  Packet context
   */
  void handle(IPayloadContext context);
}
