package slimeknights.tconstruct.common.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import slimeknights.tconstruct.common.TinkerEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Handles creating fake registry entries for datagen entries based on other mods */
public class FakeRegistryEntry {
  private static final Map<ResourceLocation, Block> BLOCKS = new HashMap<>();
  private static final Map<ResourceLocation, Item> ITEMS = new HashMap<>();
  private static final Map<ResourceLocation, MobEffect> EFFECTS = new HashMap<>();
  private static final Map<ResourceLocation, EntityType<?>> ENTITIES = new HashMap<>();

  private static <T> T getOrCreate(Map<ResourceLocation, T> cache, ResourceLocation id, Supplier<T> constructor) {
    return cache.computeIfAbsent(id, key -> constructor.get());
  }

  /** Gets or creates a fake block with the given ID */
  public static Block block(ResourceLocation id) {
    return getOrCreate(BLOCKS, id, () -> new Block(BlockBehaviour.Properties.of()));
  }

  /** Gets or creates a fake item with the given ID */
  public static Item item(ResourceLocation id) {
    return getOrCreate(ITEMS, id, () -> new Item(new Item.Properties()));
  }

  /** Gets or creates a fake mob effect with the given ID */
  public static MobEffect effect(ResourceLocation id) {
    return getOrCreate(EFFECTS, id, () -> new TinkerEffect(MobEffectCategory.NEUTRAL, false));
  }

  /** Gets or creates a fake entity with the given ID */
  public static <T extends Entity> EntityType<?> entity(ResourceLocation id) {
    return getOrCreate(ENTITIES, id, () ->
      EntityType.Builder.of((type, level) -> {
        throw new UnsupportedOperationException("Cannot create instance of fake entity");
      }, MobCategory.MISC).build(id.toString()));
  }
}
