package slimeknights.tconstruct.tools.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tools.menu.ToolContainerMenu;

/** Packet used when a fluid is changed inside a tool container menu */
public record ToolContainerFluidUpdatePacket(FluidStack fluid) implements IThreadsafePacket {
  public static final CustomPacketPayload.Type<ToolContainerFluidUpdatePacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("tool_container_fluid_update"));
  public static final StreamCodec<RegistryFriendlyByteBuf, ToolContainerFluidUpdatePacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    ToolContainerFluidUpdatePacket::new
  );

  public ToolContainerFluidUpdatePacket(FriendlyByteBuf buffer) {
    this(FluidStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf)buffer));
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    FluidStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf)buffer, fluid);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(Player contextPlayer) {
    Player player = SafeClientAccess.getPlayer();
    if (player != null && player.containerMenu instanceof ToolContainerMenu toolMenu) {
      toolMenu.getTank().setFluid(fluid);
    }
  }
}
