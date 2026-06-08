package slimeknights.tconstruct.library.tools.capability;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.function.Predicate;

/** Capability to allow an entity to store modifiers, used on projectiles fired from modifiable items */
public class EntityModifierCapability {
  /** Default instance to use when missing */
  public static final EntityModifiers EMPTY = new EntityModifiers() {
    @Override
    public ModifierNBT getModifiers() {
      return ModifierNBT.EMPTY;
    }

    @Override
    public void setModifiers(ModifierNBT nbt) {}

    @Override
    public void addModifiers(ModifierNBT nbt) {}
  };

  private EntityModifierCapability() {}

  /* Static helpers */

  /** List of predicates to check if the entity supports this capability */
  private static final List<Predicate<Entity>> ENTITY_PREDICATES = new ArrayList<>();

  /** Capability ID */
  private static final ResourceLocation ID = TConstruct.getResource("modifiers");
  /** NBT key within entity persistent data */
  private static final String NBT_KEY = ID.toString();
  /** Capability type */
  public static final EntityCapability<EntityModifiers, Void> CAPABILITY = EntityCapability.createVoid(ID, EntityModifiers.class);

  /** Cached modifier data keyed by entity instance */
  private static final Map<Entity, EntityModifiersImpl> DATA = new WeakHashMap<>();

  /** Gets the capability for the entity or an empty instance if missing */
  public static EntityModifiers getCapability(Entity entity) {
    EntityModifiers modifiers = entity.getCapability(CAPABILITY);
    return modifiers != null ? modifiers : EMPTY;
  }

  /** Gets the data or an empty instance if missing */
  public static ModifierNBT getOrEmpty(Entity entity) {
    return getCapability(entity).getModifiers();
  }

  /** Checks if the given entity supports this capability */
  public static boolean supportCapability(Entity entity) {
    for (Predicate<Entity> entityPredicate : ENTITY_PREDICATES) {
      if (entityPredicate.test(entity)) {
        return true;
      }
    }
    return false;
  }

  /** Registers a predicate of entites that need this capability */
  public static void registerEntityPredicate(Predicate<Entity> predicate) {
    ENTITY_PREDICATES.add(predicate);
  }

  /** Registers the capability with the event bus */
  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
      event.registerEntity(CAPABILITY, type, (entity, ctx) -> {
        if (supportCapability(entity)) {
          return getData(entity);
        }
        return null;
      });
    }
  }

  /** Gets or creates modifier data for the entity */
  private static EntityModifiersImpl getData(Entity entity) {
    return DATA.computeIfAbsent(entity, EntityModifierCapability::loadData);
  }

  /** Loads modifier data from entity persistent NBT */
  private static EntityModifiersImpl loadData(Entity entity) {
    CompoundTag persistent = entity.getPersistentData();
    EntityModifiersImpl impl = new EntityModifiersImpl(entity);
    if (persistent.contains(NBT_KEY, net.minecraft.nbt.Tag.TAG_LIST)) {
      impl.setModifiers(ModifierNBT.readFromNBT(persistent.getList(NBT_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)));
    }
    return impl;
  }

  /** Saves modifier data to entity persistent NBT */
  private static void saveData(Entity entity, ModifierNBT modifiers) {
    ListTag list = modifiers.serializeToNBT();
    if (list.isEmpty()) {
      entity.getPersistentData().remove(NBT_KEY);
    } else {
      entity.getPersistentData().put(NBT_KEY, list);
    }
  }

  /** Capability data instance */
  private static class EntityModifiersImpl implements EntityModifiers {
    private final Entity entity;
    @Getter @Setter
    private ModifierNBT modifiers = ModifierNBT.EMPTY;

    private EntityModifiersImpl(Entity entity) {
      this.entity = entity;
    }

    @Override
    public void setModifiers(ModifierNBT nbt) {
      this.modifiers = nbt;
      saveData(entity, nbt);
    }
  }

  /** Interface for callers to use */
  public interface EntityModifiers {
    /** Gets the stored modifiers */
    ModifierNBT getModifiers();

    /** Sets the stored modifiers */
    void setModifiers(ModifierNBT nbt);

    /** Adds additional modifiers to the stored modifiers */
    default void addModifiers(ModifierNBT nbt) {
      ModifierNBT existing = getModifiers();
      if (existing.isEmpty()) {
        setModifiers(nbt);
      } else {
        setModifiers(ModifierNBT.builder().add(existing).add(nbt).build());
      }
    }
  }
}
