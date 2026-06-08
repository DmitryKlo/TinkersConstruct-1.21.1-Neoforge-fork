package slimeknights.mantle.data.predicate.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.NamedComponentRegistry;

/** Predicate matching a specific mob type */
public record MobTypePredicate(MobTypePredicate.Type type) implements LivingEntityPredicate {
  /**
   * Registry of mob types, to allow addons to register types
   * TODO: support registering via IMC
   */
  public static final NamedComponentRegistry<Type> MOB_TYPES = new NamedComponentRegistry<>("Unknown mob type");
  /** Loader for a mob type predicate */
  public static RecordLoadable<MobTypePredicate> LOADER = RecordLoadable.create(MOB_TYPES.requiredField("mobs", MobTypePredicate::type), MobTypePredicate::new);

  public static void registerVanillaTypes() {
    MOB_TYPES.register(ResourceLocation.parse("undefined"), Type.UNDEFINED);
    MOB_TYPES.register(ResourceLocation.parse("undead"), Type.UNDEAD);
    MOB_TYPES.register(ResourceLocation.parse("arthropod"), Type.ARTHROPOD);
    MOB_TYPES.register(ResourceLocation.parse("illager"), Type.ILLAGER);
    MOB_TYPES.register(ResourceLocation.parse("water"), Type.WATER);
  }

  @Override
  public boolean matches(LivingEntity input) {
    return type.matches(input);
  }

  @Override
  public RecordLoadable<? extends LivingEntityPredicate> getLoader() {
    return LOADER;
  }

  public enum Type {
    UNDEFINED {
      @Override
      boolean matches(LivingEntity entity) {
        return !UNDEAD.matches(entity) && !ARTHROPOD.matches(entity) && !ILLAGER.matches(entity) && !WATER.matches(entity);
      }
    },
    UNDEAD {
      @Override
      boolean matches(LivingEntity entity) {
        return entity.getType().is(EntityTypeTags.UNDEAD);
      }
    },
    ARTHROPOD {
      @Override
      boolean matches(LivingEntity entity) {
        return entity.getType().is(EntityTypeTags.ARTHROPOD);
      }
    },
    ILLAGER {
      @Override
      boolean matches(LivingEntity entity) {
        return entity.getType().is(EntityTypeTags.ILLAGER);
      }
    },
    WATER {
      @Override
      boolean matches(LivingEntity entity) {
        return entity.getType().is(EntityTypeTags.AQUATIC);
      }
    };

    abstract boolean matches(LivingEntity entity);
  }
}
