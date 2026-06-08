package slimeknights.mantle.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferPacket;
import slimeknights.mantle.network.packet.DropLecternBookPacket;
import slimeknights.mantle.network.packet.OpenLecternBookPacket;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;
import slimeknights.mantle.network.packet.SwingArmPacket;
import slimeknights.mantle.network.packet.UpdateHeldPagePacket;
import slimeknights.mantle.network.packet.UpdateInventoryPagePacket;
import slimeknights.mantle.network.packet.UpdateLecternPagePacket;

public class MantleNetwork {
  /**
   * Network instance
   * 1: 1.11.101 and before
   * 2: 1.11.102 - New predicate types, enum loadable nullable field optimization
   */
  public static final NetworkWrapper INSTANCE = new NetworkWrapper(Mantle.getResource("network"), "2");

  /**
   * Registers payload handlers for this network
   */
  public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar("2");

    registrar.playToClient(OpenLecternBookPacket.TYPE, OpenLecternBookPacket.STREAM_CODEC, (packet, context) -> packet.handle(context));
    registrar.playToClient(SwingArmPacket.TYPE, SwingArmPacket.STREAM_CODEC, (packet, context) -> packet.handle(context));
    registrar.playToClient(OpenNamedBookPacket.TYPE, OpenNamedBookPacket.STREAM_CODEC, (packet, context) -> packet.handle(context));
    registrar.playToClient(FluidContainerTransferPacket.TYPE, FluidContainerTransferPacket.STREAM_CODEC, (packet, context) -> packet.handle(context));

    registrar.playToServer(UpdateHeldPagePacket.TYPE, UpdateHeldPagePacket.STREAM_CODEC, (packet, context) -> packet.handle(context));
    registrar.playToServer(UpdateInventoryPagePacket.TYPE, UpdateInventoryPagePacket.STREAM_CODEC, (packet, context) -> packet.handle(context));
    registrar.playToServer(UpdateLecternPagePacket.TYPE, UpdateLecternPagePacket.STREAM_CODEC, (packet, context) -> packet.handle(context));
    registrar.playToServer(DropLecternBookPacket.TYPE, DropLecternBookPacket.STREAM_CODEC, (packet, context) -> packet.handle(context));
  }

  /**
   * @deprecated Payloads are registered via {@link RegisterPayloadHandlersEvent}
   */
  @Deprecated
  public static void registerPackets() {}
}
