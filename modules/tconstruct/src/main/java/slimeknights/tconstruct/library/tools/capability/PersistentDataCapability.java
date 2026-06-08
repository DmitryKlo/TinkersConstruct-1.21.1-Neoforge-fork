package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.SyncPersistentDataPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Capability to store persistent NBT data on an entity. For players, this is automatically synced to the client on load, but not during gameplay.
 * Persists after death, will reassess if we need some data to not persist death
 */
public class PersistentDataCapability {
  private PersistentDataCapability() {}

  /** Capability ID */
  private static final ResourceLocation ID = TConstruct.getResource("persistent_data");
  /** NBT key within entity persistent data */
  private static final String NBT_KEY = ID.toString();
  /** Capability type */
  public static final EntityCapability<ModDataNBT, Void> CAPABILITY = EntityCapability.createVoid(ID, ModDataNBT.class);

  /** Cached data keyed by entity instance */
  private static final Map<Entity, ModDataNBT> DATA = new WeakHashMap<>();

  /** Gets the data or warns if its missing */
  public static ModDataNBT getOrWarn(Entity entity) {
    ModDataNBT data = entity.getCapability(CAPABILITY);
    if (data == null) {
      TConstruct.LOG.warn("Missing Tinkers NBT on entity {}, this should not happen", entity.getType());
      return new ModDataNBT();
    }
    return data;
  }

  /** Registers game bus event handlers */
  public static void init() {
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.Clone.class, PersistentDataCapability::playerClone);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerRespawnEvent.class, PersistentDataCapability::playerRespawn);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerChangedDimensionEvent.class, PersistentDataCapability::playerChangeDimension);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerLoggedInEvent.class, PersistentDataCapability::playerLoggedIn);
  }

  /** Registers the capability with the event bus */
  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
      event.registerEntity(CAPABILITY, type, (entity, ctx) -> {
        if (entity instanceof LivingEntity || EntityModifierCapability.supportCapability(entity)) {
          return getData(entity);
        }
        return null;
      });
    }
  }

  /** Gets or creates persistent data for the entity, backed by entity persistent NBT */
  private static ModDataNBT getData(Entity entity) {
    return DATA.computeIfAbsent(entity, PersistentDataCapability::loadData);
  }

  /** Loads data from the entity's persistent NBT tag */
  private static ModDataNBT loadData(Entity entity) {
    CompoundTag persistent = entity.getPersistentData();
    CompoundTag tag;
    if (persistent.contains(NBT_KEY, CompoundTag.TAG_COMPOUND)) {
      tag = persistent.getCompound(NBT_KEY);
    } else {
      tag = new CompoundTag();
      persistent.put(NBT_KEY, tag);
    }
    return ModDataNBT.readFromNBT(tag);
  }

  /** Syncs the data to the given player */
  private static void sync(Player player) {
    ModDataNBT data = player.getCapability(CAPABILITY);
    if (data != null) {
      TinkerNetwork.getInstance().sendTo(new SyncPersistentDataPacket(data.getCopy()), player);
    }
  }

  /** copy caps when the player respawns/returns from the end */
  private static void playerClone(PlayerEvent.Clone event) {
    ModDataNBT oldData = getOrWarn(event.getOriginal());
    CompoundTag nbt = oldData.getCopy();
    if (!nbt.isEmpty()) {
      getOrWarn(event.getEntity()).copyFrom(nbt);
    }
  }

  /** sync caps when the player respawns/returns from the end */
  private static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
    sync(event.getEntity());
  }

  /** sync caps when the player changes dimensions */
  private static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    sync(event.getEntity());
  }

  /** sync caps when the player logs in */
  private static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    sync(event.getEntity());
  }
}
