package slimeknights.mantle.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;

/**
 * Packet to update the page in a book in the players hand
 */
public record UpdateHeldPagePacket(InteractionHand hand, String page) implements IThreadsafePacket {
  private static final StreamCodec<RegistryFriendlyByteBuf, InteractionHand> HAND_CODEC = StreamCodec.of(
    (buf, hand) -> buf.writeEnum(hand),
    buf -> buf.readEnum(InteractionHand.class)
  );

  public static final CustomPacketPayload.Type<UpdateHeldPagePacket> TYPE = new CustomPacketPayload.Type<>(Mantle.getResource("update_held_page"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHeldPagePacket> STREAM_CODEC = StreamCodec.composite(
    HAND_CODEC, UpdateHeldPagePacket::hand,
    ByteBufCodecs.stringUtf8(100), UpdateHeldPagePacket::page,
    UpdateHeldPagePacket::new
  );

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    Player player = context.player();
    if (player != null && this.page != null) {
      ItemStack stack = player.getItemInHand(hand);
      if (!stack.isEmpty()) {
        BookHelper.writeSavedPageToBook(stack, this.page);
      }
    }
  }
}
