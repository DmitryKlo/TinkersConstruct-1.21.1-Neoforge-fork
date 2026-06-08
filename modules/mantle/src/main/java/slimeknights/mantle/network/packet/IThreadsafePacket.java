package slimeknights.mantle.network.packet;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet instance that automatically wraps the logic in {@link IPayloadContext#enqueueWork(Runnable)} for thread safety
 */
public interface IThreadsafePacket extends ISimplePacket {
  @Override
  default void handle(IPayloadContext context) {
    context.enqueueWork(() -> handleThreadsafe(context));
  }

  /**
   * Handles receiving the packet on the correct thread
   * @param context  Packet context
   */
  default void handleThreadsafe(IPayloadContext context) {
    handleThreadsafe(context.player());
  }

  /**
   * Legacy 1.20-style packet handler bridge.
   * @param player  Player receiving the packet context
   */
  default void handleThreadsafe(Player player) {}
}
