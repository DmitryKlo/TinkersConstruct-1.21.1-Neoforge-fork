package slimeknights.tconstruct.gadgets.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import slimeknights.tconstruct.TConstruct;

import java.util.Map;
import java.util.WeakHashMap;

/** Capability logic */
public class PiggybackCapability {
  private static final ResourceLocation ID = TConstruct.getResource("piggyback");
  public static final EntityCapability<PiggybackHandler, Void> PIGGYBACK = EntityCapability.createVoid(ID, PiggybackHandler.class);

  private static final Map<Player, PiggybackHandler> HANDLERS = new WeakHashMap<>();

  private PiggybackCapability() {}

  /** Registers the capability with the event bus */
  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerEntity(PIGGYBACK, EntityType.PLAYER, (player, ctx) -> HANDLERS.computeIfAbsent(player, PiggybackHandler::new));
  }
}
