package slimeknights.tconstruct.library.tools.layout;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.TConstruct;

import java.util.Collection;

/**
 * Packet to update the slot layouts for the tinker station
 */
@RequiredArgsConstructor
public class UpdateTinkerSlotLayoutsPacket implements IThreadsafePacket {
  public static final CustomPacketPayload.Type<UpdateTinkerSlotLayoutsPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("update_tinker_slot_layouts"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTinkerSlotLayoutsPacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    UpdateTinkerSlotLayoutsPacket::new);

  @Getter(AccessLevel.PACKAGE) @VisibleForTesting
  private final Collection<StationSlotLayout> layouts;

  public UpdateTinkerSlotLayoutsPacket(FriendlyByteBuf buffer) {
    ImmutableList.Builder<StationSlotLayout> builder = ImmutableList.builder();
    int max = buffer.readVarInt();
    for (int i = 0; i < max; i++) {
      builder.add(StationSlotLayout.read(buffer));
    }
    layouts = builder.build();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(layouts.size());
    for (StationSlotLayout layout : layouts) {
      layout.write(buffer);
    }
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    StationSlotLayoutLoader.getInstance().setSlots(layouts);
  }
}
