package slimeknights.tconstruct.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.TConstruct;

public record InventorySlotSyncPacket(ItemStack itemStack, int slot, BlockPos pos) implements IThreadsafePacket {

  private static final StreamCodec<RegistryFriendlyByteBuf, Integer> SLOT_CODEC = StreamCodec.of(
    (buf, slot) -> buf.writeShort(slot.shortValue()),
    buf -> (int) buf.readShort()
  );

  public static final CustomPacketPayload.Type<InventorySlotSyncPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("inventory_slot_sync"));
  public static final StreamCodec<RegistryFriendlyByteBuf, InventorySlotSyncPacket> STREAM_CODEC = StreamCodec.composite(
    ItemStack.OPTIONAL_STREAM_CODEC, InventorySlotSyncPacket::itemStack,
    SLOT_CODEC, InventorySlotSyncPacket::slot,
    BlockPos.STREAM_CODEC, InventorySlotSyncPacket::pos,
    InventorySlotSyncPacket::new
  );

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  private static class HandleClient {
    private static void handle(InventorySlotSyncPacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        BlockEntity te = world.getBlockEntity(packet.pos);
        if (te != null) {
          IItemHandler cap = world.getCapability(
            Capabilities.ItemHandler.BLOCK,
            packet.pos,
            te.getBlockState(),
            te,
            null
          );
          if (cap instanceof IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(packet.slot, packet.itemStack);
            Minecraft.getInstance().levelRenderer.blockChanged(null, packet.pos, null, null, 0);
          }
        }
      }
    }
  }
}
