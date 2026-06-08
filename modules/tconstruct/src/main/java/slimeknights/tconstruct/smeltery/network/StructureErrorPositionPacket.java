package slimeknights.tconstruct.smeltery.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;

import javax.annotation.Nullable;

/**
 * Packet to tell a multiblock to render a specific position as the cause of the error
 */
public record StructureErrorPositionPacket(BlockPos controllerPos, @Nullable BlockPos errorPos) implements IThreadsafePacket {
  private static final StreamCodec<RegistryFriendlyByteBuf, BlockPos> NULLABLE_BLOCK_POS = StreamCodec.of(
    (buf, pos) -> {
      buf.writeBoolean(pos != null);
      if (pos != null) {
        buf.writeBlockPos(pos);
      }
    },
    buf -> buf.readBoolean() ? buf.readBlockPos() : null
  );

  public static final CustomPacketPayload.Type<StructureErrorPositionPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("structure_error_position"));
  public static final StreamCodec<RegistryFriendlyByteBuf, StructureErrorPositionPacket> STREAM_CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, StructureErrorPositionPacket::controllerPos,
    NULLABLE_BLOCK_POS, StructureErrorPositionPacket::errorPos,
    StructureErrorPositionPacket::new
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
    private static void handle(StructureErrorPositionPacket packet) {
      BlockEntityHelper.get(HeatingStructureBlockEntity.class, Minecraft.getInstance().level, packet.controllerPos)
                       .ifPresent(te -> te.setErrorPos(packet.errorPos));
    }
  }
}
