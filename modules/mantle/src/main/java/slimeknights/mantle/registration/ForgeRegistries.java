package slimeknights.mantle.registration;

import com.mojang.serialization.MapCodec;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ForgeRegistries {
  public static final IForgeRegistry<Block> BLOCKS = wrap(BuiltInRegistries.BLOCK, Registries.BLOCK);
  public static final IForgeRegistry<Item> ITEMS = wrap(BuiltInRegistries.ITEM, Registries.ITEM);
  public static final IForgeRegistry<Fluid> FLUIDS = wrap(BuiltInRegistries.FLUID, Registries.FLUID);
  public static final IForgeRegistry<EntityType<?>> ENTITY_TYPES = wrap(BuiltInRegistries.ENTITY_TYPE, Registries.ENTITY_TYPE);
  public static final IForgeRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = wrap(BuiltInRegistries.BLOCK_ENTITY_TYPE, Registries.BLOCK_ENTITY_TYPE);
  public static final IForgeRegistry<MenuType<?>> MENU_TYPES = wrap(BuiltInRegistries.MENU, Registries.MENU);
  public static final IForgeRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = wrap(BuiltInRegistries.RECIPE_SERIALIZER, Registries.RECIPE_SERIALIZER);
  public static final IForgeRegistry<MobEffect> MOB_EFFECTS = wrap(BuiltInRegistries.MOB_EFFECT, Registries.MOB_EFFECT);
  public static final IForgeRegistry<Potion> POTIONS = wrap(BuiltInRegistries.POTION, Registries.POTION);
  public static final IForgeRegistry<Attribute> ATTRIBUTES = wrap(BuiltInRegistries.ATTRIBUTE, Registries.ATTRIBUTE);
  public static final IForgeRegistry<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = wrap(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, Registries.COMMAND_ARGUMENT_TYPE);
  public static final IForgeRegistry<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES = wrap(BuiltInRegistries.LOOT_FUNCTION_TYPE, Registries.LOOT_FUNCTION_TYPE);
  public static final IForgeRegistry<LootItemConditionType> LOOT_CONDITION_TYPES = wrap(BuiltInRegistries.LOOT_CONDITION_TYPE, Registries.LOOT_CONDITION_TYPE);
  public static final IForgeRegistry<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = wrap(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS);
  public static final IForgeRegistry<FluidType> FLUID_TYPES = wrap(NeoForgeRegistries.FLUID_TYPES, NeoForgeRegistries.Keys.FLUID_TYPES);
  public static final IForgeRegistry<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = wrap(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS);

  private ForgeRegistries() {}

  private static <T> IForgeRegistry<T> wrap(Registry<T> registry, ResourceKey<? extends Registry<T>> key) {
    return new SimpleForgeRegistry<>(registry, key);
  }

  public static final class Keys {
    public static final ResourceKey<Registry<FluidType>> FLUID_TYPES = NeoForgeRegistries.Keys.FLUID_TYPES;
    public static final ResourceKey<Registry<MapCodec<? extends IGlobalLootModifier>>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS;
    public static final ResourceKey<Registry<BiomeModifier>> BIOME_MODIFIERS = NeoForgeRegistries.Keys.BIOME_MODIFIERS;

    private Keys() {}
  }
}
