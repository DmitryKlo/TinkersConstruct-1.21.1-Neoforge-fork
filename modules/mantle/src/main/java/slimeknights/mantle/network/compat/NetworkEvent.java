package slimeknights.mantle.network.compat;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public final class NetworkEvent {
  private NetworkEvent() {}

  public static class Context {
    @Nullable
    private final ServerPlayer sender;

    public Context(@Nullable ServerPlayer sender) {
      this.sender = sender;
    }

    public void enqueueWork(Runnable runnable) {
      runnable.run();
    }

    public void setPacketHandled(boolean handled) {}

    @Nullable
    public ServerPlayer getSender() {
      return sender;
    }
  }
}
