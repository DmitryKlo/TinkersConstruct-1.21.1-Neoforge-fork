package slimeknights.mantle.fluid.transfer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.packet.IThreadsafePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Packet to sync fluid container transfer */
public record FluidContainerTransferPacket(Set<Item> items) implements IThreadsafePacket {
  public static final CustomPacketPayload.Type<FluidContainerTransferPacket> TYPE = new CustomPacketPayload.Type<>(Mantle.getResource("fluid_container_transfer"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerTransferPacket> STREAM_CODEC = StreamCodec.of(
    (buf, packet) -> {
      buf.writeVarInt(packet.items().size());
      for (Item item : packet.items()) {
        buf.writeById(BuiltInRegistries.ITEM::getId, item);
      }
    },
    buf -> {
      int size = buf.readVarInt();
      List<Item> builder = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        builder.add(buf.readById(BuiltInRegistries.ITEM::byId));
      }
      return new FluidContainerTransferPacket(Set.copyOf(builder));
    }
  );

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    FluidContainerTransferManager.INSTANCE.setContainerItems(items);
  }
}
