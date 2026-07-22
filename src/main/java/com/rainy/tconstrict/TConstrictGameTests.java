package com.rainy.tconstrict;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferManager;
import slimeknights.mantle.fluid.transfer.IFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.IFluidContainerTransfer.TransferDirection;
import slimeknights.mantle.fluid.transfer.IFluidContainerTransfer.TransferResult;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.network.InventorySlotSyncPacket;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.fluids.fluids.PotionFluidType;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.gadgets.block.PunjiBlock;
import slimeknights.tconstruct.gadgets.capability.PiggybackCapability;
import slimeknights.tconstruct.gadgets.entity.EFLNEntity;
import slimeknights.tconstruct.gadgets.entity.FancyItemFrameEntity;
import slimeknights.tconstruct.gadgets.entity.FrameType;
import slimeknights.tconstruct.gadgets.entity.GlowballEntity;
import slimeknights.tconstruct.gadgets.entity.shuriken.FlintShurikenEntity;
import slimeknights.tconstruct.gadgets.entity.shuriken.QuartzShurikenEntity;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.capability.fluid.ToolTankHelper;
import slimeknights.tconstruct.library.tools.capability.inventory.InventoryModule;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.tools.definition.module.material.MaterialRepairToolHook;
import slimeknights.tconstruct.library.tools.definition.module.mining.IsEffectiveToolHook;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionLoader;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.layout.Patterns;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.library.tools.item.ModifiableArrowItem;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.item.armor.ModifiableArmorItem;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.TinkerItemDisplays;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuelLookup;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectManager;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffects;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule.FuelInfo;
import slimeknights.tconstruct.smeltery.block.FaucetBlock;
import slimeknights.tconstruct.smeltery.block.component.OrientableSmelteryBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.smeltery.block.controller.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.HeaterBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.DrainBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.FoundryBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.MelterBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.SmelteryBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.FluidCannonBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.MeltingModuleInventory;
import slimeknights.tconstruct.smeltery.block.entity.module.MultitankFuelModule;
import slimeknights.tconstruct.smeltery.client.screen.module.GuiSmelteryTank;
import slimeknights.tconstruct.smeltery.item.CopperCanItem;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.smeltery.menu.HeatingStructureContainerMenu;
import slimeknights.tconstruct.smeltery.menu.MelterContainerMenu;
import slimeknights.tconstruct.smeltery.network.ChannelFlowPacket;
import slimeknights.tconstruct.smeltery.network.SmelteryFluidClickedPacket;
import slimeknights.tconstruct.smeltery.network.SmelteryTankUpdatePacket;
import slimeknights.tconstruct.smeltery.network.StructureErrorPositionPacket;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.entity.chest.CastChestBlockEntity;
import slimeknights.tconstruct.tables.block.entity.chest.PartChestBlockEntity;
import slimeknights.tconstruct.tables.block.entity.chest.TinkersChestBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.ModifierWorktableBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.PartBuilderBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;
import slimeknights.tconstruct.tables.menu.ModifierWorktableContainerMenu;
import slimeknights.tconstruct.tables.menu.PartBuilderContainerMenu;
import slimeknights.tconstruct.tables.menu.TinkerChestContainerMenu;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;
import slimeknights.tconstruct.tables.client.inventory.module.SlotPositionHelper;
import slimeknights.tconstruct.tables.network.TinkerStationRenamePacket;
import slimeknights.tconstruct.tables.network.TinkerStationSelectionPacket;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.TinkerToolActions;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.modules.AutosmeltModule;
import slimeknights.tconstruct.tools.modules.SmeltingModule;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.tools.entity.CombatFishingHook;
import slimeknights.tconstruct.tools.entity.CustomFireball;
import slimeknights.tconstruct.tools.entity.FluidEffectProjectile;
import slimeknights.tconstruct.tools.entity.ModifiableArrow;
import slimeknights.tconstruct.tools.entity.ThrownTool;
import slimeknights.tconstruct.tools.item.ModifierCrystalItem;
import slimeknights.tconstruct.tools.network.PushBlockRowPacket;
import slimeknights.tconstruct.tools.network.TinkerControlPacket;
import slimeknights.tconstruct.tools.network.ToolContainerFluidUpdatePacket;
import slimeknights.tconstruct.tools.recipe.ModifierSortingRecipe;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerEffects;
import slimeknights.tconstruct.shared.item.TinkerBookItem;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.TinkerHeadType;
import slimeknights.tconstruct.world.block.FoliageType;
import slimeknights.tconstruct.world.block.SlimeVineBlock;
import slimeknights.tconstruct.world.block.SlimeVineBlock.VineStage;
import slimeknights.tconstruct.world.entity.EnderSlimeEntity;
import slimeknights.tconstruct.world.entity.SkySlimeEntity;
import slimeknights.tconstruct.world.entity.TerracubeEntity;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;

@PrefixGameTestTemplate(false)
public final class TConstrictGameTests {
    private TConstrictGameTests() {}

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 20)
    public static void smelteryFuelFallbacksAreAvailable(GameTestHelper helper) {
        MeltingFuel lava = MeltingFuelLookup.findFuel(Fluids.LAVA);
        helper.assertTrue(lava != null, "Lava must be accepted as smeltery fuel while generated fuel recipes are absent");
        helper.assertTrue(lava.getAmount(Fluids.LAVA) == 50, "Lava fallback must consume 50 mB per fuel cycle");
        helper.assertTrue(lava.getDuration() == 100, "Lava fallback duration must match the port fallback");
        helper.assertTrue(lava.getTemperature() >= 1000, "Lava fallback must be hot enough for basic smeltery recipes");
        helper.assertTrue(lava.getRate() > 0, "Lava fallback must heat melting slots");

        MeltingFuel blazingBlood = MeltingFuelLookup.findFuel(TinkerFluids.blazingBlood.get());
        helper.assertTrue(blazingBlood != null, "Blazing blood must be accepted as high temperature smeltery fuel");
        helper.assertTrue(blazingBlood.getTemperature() >= 1500, "Blazing blood fallback must be hotter than lava");
        helper.assertTrue(blazingBlood.getRate() > lava.getRate(), "Blazing blood fallback should heat faster than lava");

        MeltingFuel solid = MeltingFuelLookup.getSolid();
        helper.assertTrue(solid.getTemperature() >= 800, "Solid fallback fuel must be available for the melter/heater");
        helper.assertTrue(solid.getRate() > 0, "Solid fallback fuel must heat melting slots");

        helper.assertTrue(MeltingFuelLookup.findFuel(Fluids.WATER) == null, "Water must not be accepted as smeltery fuel");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 800)
    public static void searedMelterHeatsIronIntoMoltenIron(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos heaterPos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos melterPos = heaterPos.above();

        level.setBlock(heaterPos, TinkerSmeltery.searedHeater.get().defaultBlockState().setValue(ControllerBlock.IN_STRUCTURE, true), Block.UPDATE_ALL);
        level.setBlock(melterPos, TinkerSmeltery.searedMelter.get().defaultBlockState().setValue(ControllerBlock.IN_STRUCTURE, true), Block.UPDATE_ALL);

        HeaterBlockEntity heater = (HeaterBlockEntity) level.getBlockEntity(heaterPos);
        helper.assertTrue(heater != null, "Seared heater must create a block entity");
        heater.getItemHandler().setStackInSlot(0, new ItemStack(Items.COAL));

        MelterBlockEntity melter = (MelterBlockEntity) level.getBlockEntity(melterPos);
        helper.assertTrue(melter != null, "Seared melter must create a block entity");
        helper.assertTrue(new ItemStack(Items.IRON_INGOT).is(Tags.Items.INGOTS_IRON),
            "minecraft:iron_ingot must be present in " + Tags.Items.INGOTS_IRON.location());
        long tconstructRecipeIds = level.getRecipeManager().getRecipeIds().filter(id -> id.getNamespace().equals("tconstruct")).count();
        var ironRecipe = level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("tconstruct", "smeltery/melting/metal/iron/ingot")).orElse(null);
        String ironRecipeState = ironRecipe == null ? "missing" : ironRecipe.value().getClass().getName() + " type=" + ironRecipe.value().getType();
        int meltingRecipeCount = level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.MELTING.get()).size();
        melter.getMeltingInventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
        long matchingMeltingRecipes = level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.MELTING.get()).stream()
            .filter(holder -> holder.value().matches(melter.getMeltingInventory().getModule(0), level))
            .count();
        helper.assertTrue(melter.getMeltingInventory().getRequiredTime(0) > 0,
            "Iron ingot must resolve a melting recipe; requiredTime=" + melter.getMeltingInventory().getRequiredTime(0)
                + ", requiredTemp=" + melter.getMeltingInventory().getRequiredTemp(0)
                + ", meltingRecipeCount=" + meltingRecipeCount
                + ", matchingMeltingRecipes=" + matchingMeltingRecipes
                + ", tconstructRecipeIds=" + tconstructRecipeIds
                + ", ironRecipe=" + ironRecipeState);

        helper.runAfterDelay(700, () -> {
            FluidStack fluid = melter.getTank().getFluid();
            String state = "fluid=" + BuiltInRegistries.FLUID.getKey(fluid.getFluid())
                + " amount=" + fluid.getAmount()
                + " input=" + melter.getMeltingInventory().getStackInSlot(0)
                + " time=" + melter.getMeltingInventory().getCurrentTime(0) + "/" + melter.getMeltingInventory().getRequiredTime(0)
                + " temp=" + melter.getMeltingInventory().getRequiredTemp(0)
                + " fuel=" + melter.getFuelModule().getFuel()
                + " fuelTemp=" + melter.getFuelModule().getTemperature()
                + " rate=" + melter.getFuelModule().getRate()
                + " heaterFuel=" + heater.getItemHandler().getStackInSlot(0);
            helper.assertTrue(fluid.getFluid() == TinkerFluids.moltenIron.get(), "Melter must output molten iron from an iron ingot; " + state);
            helper.assertTrue(fluid.getAmount() >= 90, "Melter must output at least one ingot of molten iron; " + state);
            helper.assertTrue(melter.getMeltingInventory().getStackInSlot(0).isEmpty(), "Melter input slot must be consumed after melting; " + state);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 100)
    public static void searedMelterOpensMenuWhenClickedWithItem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos heaterPos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos melterPos = heaterPos.above();

        level.setBlock(heaterPos, TinkerSmeltery.searedHeater.get().defaultBlockState().setValue(ControllerBlock.IN_STRUCTURE, true), Block.UPDATE_ALL);
        level.setBlock(melterPos, TinkerSmeltery.searedMelter.get().defaultBlockState().setValue(ControllerBlock.IN_STRUCTURE, true), Block.UPDATE_ALL);

        MelterBlockEntity melter = (MelterBlockEntity) level.getBlockEntity(melterPos);
        helper.assertTrue(melter != null, "Seared melter must create a block entity before opening its menu");
        MenuType<?> expectedMenuType = TinkerSmeltery.melterContainer.get();
        helper.assertTrue(melter.createMenu(7, null, null) instanceof MelterContainerMenu, "Seared melter block entity must create a melter menu");

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
        helper.useBlock(new BlockPos(1, 2, 1), player);
        helper.assertTrue(melter.createMenu(8, player.getInventory(), player).getType() == expectedMenuType,
            "Clicking a formed seared melter while holding an item must be able to resolve the melter menu type");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 20)
    public static void meltingInventoryIgnoresProgressOnlySlotTags(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos melterPos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(melterPos, TinkerSmeltery.searedMelter.get().defaultBlockState().setValue(ControllerBlock.IN_STRUCTURE, true), Block.UPDATE_ALL);

        MelterBlockEntity melter = (MelterBlockEntity) level.getBlockEntity(melterPos);
        helper.assertTrue(melter != null, "Seared melter must create a block entity");
        MeltingModuleInventory inventory = melter.getMeltingInventory();
        inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));

        CompoundTag progressOnly = new CompoundTag();
        progressOnly.putByte("slot", (byte)0);
        progressOnly.putInt("time", 10);
        progressOnly.putInt("required", 900);
        progressOnly.putInt("temp", 800);
        ListTag items = new ListTag();
        items.add(progressOnly);
        CompoundTag root = new CompoundTag();
        root.put("items", items);
        root.putByte("size", (byte)3);

        inventory.readFromTag(root);
        helper.assertTrue(inventory.getStackInSlot(0).isEmpty(),
            "Progress-only melting slot tags without an item id must be ignored instead of parsed as ItemStacks");
        helper.assertTrue(inventory.getCurrentTime(0) == 0 && inventory.getRequiredTime(0) == 0 && inventory.getRequiredTemp(0) == 0,
            "Ignoring a progress-only melting slot tag must clear stale melting progress; time=" + inventory.getCurrentTime(0)
                + ", required=" + inventory.getRequiredTime(0) + ", temp=" + inventory.getRequiredTemp(0));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 20)
    public static void startupDataReloadPopulatesCoreTinkeringRegistries(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        IMaterialRegistry materials = MaterialRegistry.getInstance();

        helper.assertTrue(MaterialRegistry.getMaterials().size() >= 70,
            "Material reload must populate the visible material registry; count=" + MaterialRegistry.getMaterials().size());
        helper.assertTrue(materials.getAllMaterials().size() >= MaterialRegistry.getMaterials().size(),
            "All material registry entries must include visible materials; all=" + materials.getAllMaterials().size()
                + ", visible=" + MaterialRegistry.getMaterials().size());
        helper.assertTrue(materials.getMaterial(MaterialIds.iron) != slimeknights.tconstruct.library.materials.definition.IMaterial.UNKNOWN,
            "Runtime material registry must resolve the upstream iron material");
        helper.assertTrue(materials.getAllStatTypeIds().size() >= 15,
            "Material stat type registry must keep the tool/armor/ammo stat families; count=" + materials.getAllStatTypeIds().size());
        helper.assertTrue(materials.getMaterialStats(MaterialIds.iron, HeadMaterialStats.ID).isPresent(),
            "Iron material must load head stats for melee/harvest tools");
        helper.assertTrue(!materials.getTraits(MaterialIds.iron, HeadMaterialStats.ID).isEmpty(),
            "Iron material must load head traits from material trait data");

        long modifierCount = ModifierManager.INSTANCE.getAllLocations().count();
        long modifierTagCount = ModifierManager.getAllTags().count();
        helper.assertTrue(modifierCount >= 250,
            "Dynamic/static modifier reload must populate modifier ids; count=" + modifierCount);
        helper.assertTrue(modifierTagCount >= 20,
            "Modifier tag reload must populate modifier tags; count=" + modifierTagCount);
        helper.assertTrue(ModifierManager.INSTANCE.contains(ModifierIds.diamond) && ModifierManager.INSTANCE.contains(ModifierIds.sharpness),
            "Runtime modifier manager must resolve representative upstream modifiers");

        long loadedDefinitions = ToolDefinitionLoader.getInstance().getRegisteredToolDefinitions().stream()
            .filter(definition -> definition.isDataLoaded())
            .count();
        helper.assertTrue(loadedDefinitions >= 40,
            "Tool definition reload must populate registered tool definitions; loaded=" + loadedDefinitions
                + ", registered=" + ToolDefinitionLoader.getInstance().getRegisteredToolDefinitions().size());
        helper.assertTrue(TinkerTools.dagger.get().getToolDefinition().isDataLoaded()
                && TinkerTools.pickaxe.get().getToolDefinition().isDataLoaded(),
            "Representative dagger and pickaxe tool definitions must have datapack data loaded");

        StationSlotLayoutLoader layouts = StationSlotLayoutLoader.getInstance();
        helper.assertTrue(layouts.getSortedSlots().size() >= 20,
            "Station layout reload must populate selectable station layouts; count=" + layouts.getSortedSlots().size());
        helper.assertTrue(!layouts.get(TinkerTables.tinkerStation.getId()).getInputSlots().isEmpty()
                && !layouts.get(TinkerTables.tinkersAnvil.getId()).getInputSlots().isEmpty(),
            "Required tinker station and anvil layouts must load input slots");

        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.MATERIAL.get()).size() >= 1,
            "Material recipe reload must populate material recipes; count=" + level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.MATERIAL.get()).size());
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.PART_BUILDER.get()).size() >= 1,
            "Part builder recipe reload must populate material part recipes; count=" + level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.PART_BUILDER.get()).size());
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.TINKER_STATION.get()).size() >= 10,
            "Tinker station recipe reload must populate tool/modifier recipes; count=" + level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.TINKER_STATION.get()).size());
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.MELTING.get()).size() >= 1,
            "Melting recipe reload must populate smeltery recipes; count=" + level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.MELTING.get()).size());
        helper.assertTrue(level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("tconstruct", "smeltery/melting/metal/iron/ingot")).isPresent(),
            "Melting recipe reload must include the representative upstream iron ingot recipe");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 20)
    public static void bookAssetsPackagedForRegisteredTinkerBooks(GameTestHelper helper) {
        helper.assertTrue(TinkerCommons.materialsAndYou.get() instanceof TinkerBookItem, "Materials and You item must be a Tinker book item");
        helper.assertTrue(TinkerCommons.punySmelting.get() instanceof TinkerBookItem, "Puny Smelting item must be a Tinker book item");
        helper.assertTrue(TinkerCommons.mightySmelting.get() instanceof TinkerBookItem, "Mighty Smelting item must be a Tinker book item");
        helper.assertTrue(TinkerCommons.tinkersGadgetry.get() instanceof TinkerBookItem, "Tinker's Gadgetry item must be a Tinker book item");
        helper.assertTrue(TinkerCommons.fantasticFoundry.get() instanceof TinkerBookItem, "Fantastic Foundry item must be a Tinker book item");
        helper.assertTrue(TinkerCommons.encyclopedia.get() instanceof TinkerBookItem, "Encyclopedia item must be a Tinker book item");

        assertBookRootAssets(helper, "materials_and_you");
        assertBookRootAssets(helper, "puny_smelting");
        assertBookRootAssets(helper, "mighty_smelting");
        assertBookRootAssets(helper, "tinkers_gadgetry");
        assertBookRootAssets(helper, "fantastic_foundry");
        assertBookRootAssets(helper, "encyclopedia");

        assertBookBinaryResource(helper, "images/book_mighty_smelting.png", 1024);
        assertBookBinaryResource(helper, "images/piggybackpack.png", 1024);
        assertBookBinaryResource(helper, "images/foundrygui.png", 1024);
        assertBookBinaryResource(helper, "structures/smeltery.nbt", 256);
        assertBookBinaryResource(helper, "structures/foundry.nbt", 256);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 20)
    public static void jeiPluginKeepsCoreCategoriesAndCatalystsWired(GameTestHelper helper) {
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.JEIPlugin", 1024);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.TConstructJEIConstants", 1024);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.casting.CastingBasinCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.casting.CastingTableCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.MoldingRecipeCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.melting.MeltingCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.melting.FoundryCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.AlloyRecipeCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.entity.EntityMeltingRecipeCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.entity.SeveringCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.ToolBuildingCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.partbuilder.PartBuilderCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.modifiers.ModifierRecipeCategory", 512);
        assertClassResource(helper, "slimeknights.tconstruct.plugin.jei.modifiers.ModifierWorktableCategory", 512);

        String constants = readClassResourceText(helper, "slimeknights.tconstruct.plugin.jei.TConstructJEIConstants", 1024);
        assertSourceContains(helper, constants, "casting_basin", "JEI constants must keep the casting basin recipe type");
        assertSourceContains(helper, constants, "casting_table", "JEI constants must keep the casting table recipe type");
        assertSourceContains(helper, constants, "molding", "JEI constants must keep the molding recipe type");
        assertSourceContains(helper, constants, "melting", "JEI constants must keep the melting recipe type");
        assertSourceContains(helper, constants, "entity_melting", "JEI constants must keep the entity melting recipe type");
        assertSourceContains(helper, constants, "alloy", "JEI constants must keep the alloy recipe type");
        assertSourceContains(helper, constants, "foundry", "JEI constants must keep the foundry recipe type");
        assertSourceContains(helper, constants, "modifiers", "JEI constants must keep the modifiers recipe type");
        assertSourceContains(helper, constants, "severing", "JEI constants must keep the severing recipe type");
        assertSourceContains(helper, constants, "tool_recipes", "JEI constants must keep the tool building recipe type");
        assertSourceContains(helper, constants, "part_builder", "JEI constants must keep the part builder recipe type");
        assertSourceContains(helper, constants, "worktable", "JEI constants must keep the modifier worktable recipe type");

        String plugin = readClassResourceText(helper, "slimeknights.tconstruct.plugin.jei.JEIPlugin", 1024);
        assertSourceContains(helper, plugin, "CastingBasinCategory", "JEI plugin must register casting basin category");
        assertSourceContains(helper, plugin, "CastingTableCategory", "JEI plugin must register casting table category");
        assertSourceContains(helper, plugin, "MeltingCategory", "JEI plugin must register melting category");
        assertSourceContains(helper, plugin, "FoundryCategory", "JEI plugin must register foundry category");
        assertSourceContains(helper, plugin, "PartBuilderCategory", "JEI plugin must register part builder category");
        assertSourceContains(helper, plugin, "ModifierWorktableCategory", "JEI plugin must register modifier worktable category");
        assertSourceContains(helper, plugin, "smelteryController", "JEI plugin must keep smeltery controller catalysts");
        assertSourceContains(helper, plugin, "foundryController", "JEI plugin must keep foundry controller catalysts");
        assertSourceContains(helper, plugin, "tinkerStation", "JEI plugin must keep tinker station catalysts");
        assertSourceContains(helper, plugin, "modifierWorktable", "JEI plugin must keep modifier worktable catalysts");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 100)
    public static void smelteryAndFoundryControllersCreateMenus(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos smelteryPos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos foundryPos = helper.absolutePos(new BlockPos(5, 1, 1));

        level.setBlock(smelteryPos, TinkerSmeltery.smelteryController.get().defaultBlockState().setValue(ControllerBlock.IN_STRUCTURE, true), Block.UPDATE_ALL);
        level.setBlock(foundryPos, TinkerSmeltery.foundryController.get().defaultBlockState().setValue(ControllerBlock.IN_STRUCTURE, true), Block.UPDATE_ALL);

        assertHeatingControllerMenu(helper, (SmelteryBlockEntity) level.getBlockEntity(smelteryPos), smelteryPos, "Smeltery");
        assertHeatingControllerMenu(helper, (FoundryBlockEntity) level.getBlockEntity(foundryPos), foundryPos, "Foundry");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 100)
    public static void smelteryFormsRealMinimalStructure(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos smelteryController = helper.absolutePos(new BlockPos(2, 2, 1));

        buildMinimalHeatingStructure(level, smelteryController, true);

        SmelteryBlockEntity smeltery = (SmelteryBlockEntity) level.getBlockEntity(smelteryController);
        helper.assertTrue(smeltery != null, "Smeltery controller must create a block entity in a real structure");

        smeltery.updateStructure();

        helper.runAfterDelay(2, () -> {
            assertRealHeatingStructure(helper, smeltery, smelteryController, "Smeltery");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 100)
    public static void foundryFormsRealMinimalStructure(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos foundryController = helper.absolutePos(new BlockPos(2, 2, 1));

        buildMinimalHeatingStructure(level, foundryController, false);

        FoundryBlockEntity foundry = (FoundryBlockEntity) level.getBlockEntity(foundryController);
        helper.assertTrue(foundry != null, "Foundry controller must create a block entity in a real structure");

        foundry.updateStructure();

        helper.runAfterDelay(2, () -> {
            assertRealHeatingStructure(helper, foundry, foundryController, "Foundry");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 900)
    public static void realSmelteryHeatsIronIntoMoltenIron(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos controllerPos = helper.absolutePos(new BlockPos(2, 2, 1));

        buildMinimalHeatingStructure(level, controllerPos, true);

        SmelteryBlockEntity smeltery = (SmelteryBlockEntity) level.getBlockEntity(controllerPos);
        helper.assertTrue(smeltery != null, "Smeltery controller must create a block entity in a real melting test");
        smeltery.updateStructure();

        helper.runAfterDelay(2, () -> {
            assertRealHeatingStructure(helper, smeltery, controllerPos, "Smeltery");

            int filled = smeltery.getFuelModule().fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE);
            helper.assertTrue(filled > 0, "Real smeltery fuel module must accept lava; filled=" + filled);
            helper.assertTrue(smeltery.getFuelModule().getTanks() > 0, "Real smeltery must expose at least one fuel tank");

            smeltery.getMeltingInventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
            helper.assertTrue(smeltery.getMeltingInventory().getRequiredTime(0) > 0,
                "Real smeltery must resolve an iron ingot melting recipe; requiredTime="
                    + smeltery.getMeltingInventory().getRequiredTime(0)
                    + ", requiredTemp=" + smeltery.getMeltingInventory().getRequiredTemp(0));
        });

        helper.runAfterDelay(720, () -> {
            FluidStack fluid = smeltery.getTank().getFluidInTank(0);
            String state = "fluid=" + BuiltInRegistries.FLUID.getKey(fluid.getFluid())
                + " amount=" + fluid.getAmount()
                + " input=" + smeltery.getMeltingInventory().getStackInSlot(0)
                + " time=" + smeltery.getMeltingInventory().getCurrentTime(0) + "/" + smeltery.getMeltingInventory().getRequiredTime(0)
                + " temp=" + smeltery.getMeltingInventory().getRequiredTemp(0)
                + " fuel=" + smeltery.getFuelModule().getFuel()
                + " fuelTemp=" + smeltery.getFuelModule().getTemperature()
                + " rate=" + smeltery.getFuelModule().getRate()
                + " tanks=" + smeltery.getFuelModule().getTanks()
                + " fuelFluid=" + smeltery.getFuelModule().getFluidInTank(0);
            helper.assertTrue(fluid.getFluid() == TinkerFluids.moltenIron.get(), "Real smeltery must output molten iron from an iron ingot; " + state);
            helper.assertTrue(fluid.getAmount() >= 90, "Real smeltery must output at least one ingot of molten iron; " + state);
            helper.assertTrue(smeltery.getMeltingInventory().getStackInSlot(0).isEmpty(), "Real smeltery input slot must be consumed after melting; " + state);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 1500)
    public static void realSmelteryHeatsRawGoldAndGoldOreIntoMoltenGold(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos controllerPos = helper.absolutePos(new BlockPos(2, 2, 1));

        buildMinimalHeatingStructure(level, controllerPos, true);

        SmelteryBlockEntity smeltery = (SmelteryBlockEntity) level.getBlockEntity(controllerPos);
        helper.assertTrue(smeltery != null, "Smeltery controller must create a block entity for gold melting regression");
        smeltery.updateStructure();

        helper.runAfterDelay(2, () -> {
            assertRealHeatingStructure(helper, smeltery, controllerPos, "Gold smeltery");

            int filled = smeltery.getFuelModule().fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE);
            helper.assertTrue(filled > 0, "Gold smeltery fuel module must accept lava; filled=" + filled);

            MeltingModuleInventory inventory = smeltery.getMeltingInventory();
            inventory.setStackInSlot(0, new ItemStack(Items.RAW_GOLD));
            helper.assertTrue(inventory.getRequiredTime(0) > 0 && inventory.getRequiredTemp(0) > 0,
                "Raw gold must resolve a loaded TConstruct melting recipe; time=" + inventory.getRequiredTime(0)
                    + ", temp=" + inventory.getRequiredTemp(0));
        });

        helper.runAfterDelay(420, () -> {
            MeltingModuleInventory inventory = smeltery.getMeltingInventory();
            FluidStack fluid = smeltery.getTank().getFluidInTank(0);
            String state = "fluid=" + BuiltInRegistries.FLUID.getKey(fluid.getFluid())
                + " amount=" + fluid.getAmount()
                + " rawInput=" + inventory.getStackInSlot(0)
                + " rawTime=" + inventory.getCurrentTime(0) + "/" + inventory.getRequiredTime(0)
                + " temp=" + inventory.getRequiredTemp(0);
            helper.assertTrue(fluid.getFluid() == TinkerFluids.moltenGold.get() && fluid.getAmount() >= FluidValues.INGOT,
                "Smeltery must output molten gold from raw gold before testing gold ore; " + state);
            helper.assertTrue(inventory.getStackInSlot(0).isEmpty(),
                "Raw gold input slot must be consumed before inserting gold ore; " + state);

            inventory.setStackInSlot(0, new ItemStack(Items.GOLD_ORE));
            helper.assertTrue(inventory.getRequiredTime(0) > 0 && inventory.getRequiredTemp(0) > 0,
                "Gold ore block must resolve a loaded TConstruct melting recipe; time=" + inventory.getRequiredTime(0)
                    + ", temp=" + inventory.getRequiredTemp(0));
        });

        helper.runAfterDelay(1200, () -> {
            FluidStack fluid = smeltery.getTank().getFluidInTank(0);
            String state = "fluid=" + BuiltInRegistries.FLUID.getKey(fluid.getFluid())
                + " amount=" + fluid.getAmount()
                + " input=" + smeltery.getMeltingInventory().getStackInSlot(0)
                + " time=" + smeltery.getMeltingInventory().getCurrentTime(0) + "/" + smeltery.getMeltingInventory().getRequiredTime(0)
                + " temp=" + smeltery.getMeltingInventory().getRequiredTemp(0);
            helper.assertTrue(fluid.getFluid() == TinkerFluids.moltenGold.get(),
                "Smeltery must output molten gold from raw gold and gold ore; " + state);
            helper.assertTrue(fluid.getAmount() >= FluidValues.INGOT * 3,
                "Gold melting must produce at least three ingots worth of molten gold for raw+ore inputs; " + state);
            helper.assertTrue(smeltery.getMeltingInventory().getStackInSlot(0).isEmpty(),
                "Gold smeltery input slots must be consumed after melting; " + state);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 900)
    public static void realFoundryHeatsIronIntoMoltenIron(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos controllerPos = helper.absolutePos(new BlockPos(2, 2, 1));

        buildMinimalHeatingStructure(level, controllerPos, false);

        FoundryBlockEntity foundry = (FoundryBlockEntity) level.getBlockEntity(controllerPos);
        helper.assertTrue(foundry != null, "Foundry controller must create a block entity in a real melting test");
        foundry.updateStructure();

        helper.runAfterDelay(2, () -> {
            assertRealHeatingStructure(helper, foundry, controllerPos, "Foundry");

            int filled = foundry.getFuelModule().fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE);
            helper.assertTrue(filled > 0, "Real foundry fuel module must accept lava; filled=" + filled);
            helper.assertTrue(foundry.getFuelModule().getTanks() > 0, "Real foundry must expose at least one fuel tank");

            foundry.getMeltingInventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
            helper.assertTrue(foundry.getMeltingInventory().getRequiredTime(0) > 0,
                "Real foundry must resolve an iron ingot melting recipe; requiredTime="
                    + foundry.getMeltingInventory().getRequiredTime(0)
                    + ", requiredTemp=" + foundry.getMeltingInventory().getRequiredTemp(0));
        });

        helper.runAfterDelay(720, () -> {
            FluidStack fluid = foundry.getTank().getFluidInTank(0);
            String state = "fluid=" + BuiltInRegistries.FLUID.getKey(fluid.getFluid())
                + " amount=" + fluid.getAmount()
                + " input=" + foundry.getMeltingInventory().getStackInSlot(0)
                + " time=" + foundry.getMeltingInventory().getCurrentTime(0) + "/" + foundry.getMeltingInventory().getRequiredTime(0)
                + " temp=" + foundry.getMeltingInventory().getRequiredTemp(0)
                + " fuel=" + foundry.getFuelModule().getFuel()
                + " fuelTemp=" + foundry.getFuelModule().getTemperature()
                + " rate=" + foundry.getFuelModule().getRate()
                + " tanks=" + foundry.getFuelModule().getTanks()
                + " fuelFluid=" + foundry.getFuelModule().getFluidInTank(0);
            helper.assertTrue(fluid.getFluid() == TinkerFluids.moltenIron.get(), "Real foundry must output molten iron from an iron ingot; " + state);
            helper.assertTrue(fluid.getAmount() >= 90, "Real foundry must output at least one ingot of molten iron; " + state);
            helper.assertTrue(foundry.getMeltingInventory().getStackInSlot(0).isEmpty(), "Real foundry input slot must be consumed after melting; " + state);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void smelteryFuelUiAggregatesAllFuelTanksByFluid(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos controllerPos = helper.absolutePos(new BlockPos(2, 2, 1));
        BlockPos lavaTankPos = controllerPos.offset(1, 0, 1);
        BlockPos secondLavaTankPos = controllerPos.offset(0, 0, 2);
        BlockPos bloodTankPos = controllerPos.offset(-1, 0, 1);

        buildMinimalHeatingStructure(level, controllerPos, true);
        level.setBlock(secondLavaTankPos, TinkerSmeltery.searedTank.get(TankType.FUEL_TANK).defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(bloodTankPos, TinkerSmeltery.searedTank.get(TankType.FUEL_TANK).defaultBlockState(), Block.UPDATE_ALL);

        SmelteryBlockEntity smeltery = (SmelteryBlockEntity) level.getBlockEntity(controllerPos);
        helper.assertTrue(smeltery != null, "Smeltery controller must create a block entity for fuel UI aggregation");
        smeltery.updateStructure();

        helper.runAfterDelay(2, () -> {
            assertRealHeatingStructure(helper, smeltery, controllerPos, "Smeltery fuel UI aggregation");

            IFluidHandler lavaTank = level.getCapability(Capabilities.FluidHandler.BLOCK, lavaTankPos, level.getBlockState(lavaTankPos), level.getBlockEntity(lavaTankPos), null);
            IFluidHandler secondLavaTank = level.getCapability(Capabilities.FluidHandler.BLOCK, secondLavaTankPos, level.getBlockState(secondLavaTankPos), level.getBlockEntity(secondLavaTankPos), null);
            IFluidHandler bloodTank = level.getCapability(Capabilities.FluidHandler.BLOCK, bloodTankPos, level.getBlockState(bloodTankPos), level.getBlockEntity(bloodTankPos), null);
            helper.assertTrue(lavaTank != null, "First fuel tank must expose a fluid handler");
            helper.assertTrue(secondLavaTank != null, "Second lava fuel tank must expose a fluid handler");
            helper.assertTrue(bloodTank != null, "Second fuel tank must expose a fluid handler");

            int lavaFilled = lavaTank.fill(new FluidStack(Fluids.LAVA, 600), FluidAction.EXECUTE);
            int secondLavaFilled = secondLavaTank.fill(new FluidStack(Fluids.LAVA, 400), FluidAction.EXECUTE);
            int bloodFilled = bloodTank.fill(new FluidStack(TinkerFluids.blazingBlood.get(), 500), FluidAction.EXECUTE);
            helper.assertTrue(lavaFilled == 600, "First tank must accept exactly 600 mB lava; filled=" + lavaFilled);
            helper.assertTrue(secondLavaFilled == 400, "Second lava tank must accept exactly 400 mB lava; filled=" + secondLavaFilled);
            helper.assertTrue(bloodFilled == 500, "Second tank must accept exactly 500 mB blazing blood; filled=" + bloodFilled);

            List<FuelInfo> fuels = smeltery.getFuelModule().getFuelInfos();
            FuelInfo lavaInfo = findFuelInfo(fuels, Fluids.LAVA);
            FuelInfo bloodInfo = findFuelInfo(fuels, TinkerFluids.blazingBlood.get());
            int totalCapacity = lavaTank.getTankCapacity(0) + secondLavaTank.getTankCapacity(0) + bloodTank.getTankCapacity(0);
            helper.assertTrue(lavaInfo != null, "Fuel UI must include a lava group; fuels=" + fuels);
            helper.assertTrue(bloodInfo != null, "Fuel UI must include a blazing blood group; fuels=" + fuels);
            helper.assertTrue(lavaInfo.getTotalAmount() == 1000, "Lava UI group must sum lava across multiple fuel tanks; amount=" + lavaInfo.getTotalAmount());
            helper.assertTrue(bloodInfo.getTotalAmount() == 500, "Blazing blood UI group must sum blazing blood across all fuel tanks; amount=" + bloodInfo.getTotalAmount());
            helper.assertTrue(lavaInfo.getCapacity() == totalCapacity, "Lava UI group must use total fuel tank capacity; capacity=" + lavaInfo.getCapacity() + ", expected=" + totalCapacity);
            helper.assertTrue(bloodInfo.getCapacity() == totalCapacity, "Blazing blood UI group must use total fuel tank capacity; capacity=" + bloodInfo.getCapacity() + ", expected=" + totalCapacity);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 20)
    public static void smelteryFuelGuiHeightsKeepSmallMixedFluidsVisible(GameTestHelper helper) {
        List<FluidStack> tinyMixedFuel = List.of(
            new FluidStack(Fluids.LAVA, 1),
            new FluidStack(TinkerFluids.blazingBlood.get(), 1)
        );
        int[] tinyHeights = GuiSmelteryTank.calcLiquidHeights(tinyMixedFuel, 12000, 90, 1);
        helper.assertTrue(tinyHeights.length == 2, "Fuel GUI height array must preserve one entry per visible fuel fluid");
        helper.assertTrue(tinyHeights[0] > 0 && tinyHeights[1] > 0,
            "Tiny mixed fuel fluids must each remain visible instead of one disappearing; heights=" + tinyHeights[0] + "," + tinyHeights[1]);
        helper.assertTrue(tinyHeights[0] + tinyHeights[1] <= 90,
            "Tiny mixed fuel heights must stay inside the fuel tank bounds; sum=" + (tinyHeights[0] + tinyHeights[1]));

        List<FluidStack> mixedFuel = List.of(
            new FluidStack(Fluids.LAVA, 1000),
            new FluidStack(TinkerFluids.blazingBlood.get(), 500)
        );
        int[] mixedHeights = GuiSmelteryTank.calcLiquidHeights(mixedFuel, 12000, 90, 1);
        helper.assertTrue(mixedHeights[0] == 8 && mixedHeights[1] == 4,
            "Mixed fuel GUI heights must use proportional sub-bucket amounts instead of coarse bucket rounding; heights="
                + mixedHeights[0] + "," + mixedHeights[1]);

        List<FluidStack> overfullResults = List.of(
            new FluidStack(TinkerFluids.moltenIron.get(), 9000),
            new FluidStack(TinkerFluids.moltenGold.get(), 9000),
            new FluidStack(TinkerFluids.moltenCopper.get(), 9000)
        );
        int[] overfullHeights = GuiSmelteryTank.calcLiquidHeights(overfullResults, 12000, 106, 1);
        int overfullSum = overfullHeights[0] + overfullHeights[1] + overfullHeights[2];
        helper.assertTrue(overfullSum == 106,
            "Overfull result-fluid GUI heights must be clamped exactly to the tank height; sum=" + overfullSum);
        helper.assertTrue(overfullHeights[0] > 0 && overfullHeights[1] > 0 && overfullHeights[2] > 0,
            "Overfull result-fluid GUI must keep all non-empty result fluids visible; heights="
                + overfullHeights[0] + "," + overfullHeights[1] + "," + overfullHeights[2]);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 20)
    public static void smelteryFluidGuiHeightsClampRectangularTanks(GameTestHelper helper) {
        List<FluidStack> fullRectangularTank = List.of(new FluidStack(TinkerFluids.moltenGold.get(), 4000));
        int[] fullHeights = GuiSmelteryTank.calcLiquidHeights(fullRectangularTank, 4000, 32, 1);
        helper.assertTrue(fullHeights.length == 1 && fullHeights[0] == 32,
            "Rectangular result tank rendering must scale to height, not width; height=" + (fullHeights.length == 0 ? -1 : fullHeights[0]));

        List<FluidStack> overfullRectangularTank = List.of(
            new FluidStack(TinkerFluids.moltenGold.get(), 3000),
            new FluidStack(TinkerFluids.moltenIron.get(), 3000)
        );
        int[] overfullHeights = GuiSmelteryTank.calcLiquidHeights(overfullRectangularTank, 4000, 32, 1);
        helper.assertTrue(overfullHeights[0] + overfullHeights[1] == 32,
            "Overfull rectangular result tank rendering must clamp to the visible tank height; heights="
                + overfullHeights[0] + "," + overfullHeights[1]);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 20)
    public static void slotPositionHelperMovesFinalSlotCoordinates(GameTestHelper helper) {
        Slot slot = new Slot(new net.minecraft.world.SimpleContainer(1), 0, 1, 2);
        SlotPositionHelper.setSlotPosition(slot, 41, 59);
        helper.assertTrue(slot.x == 41 && slot.y == 59,
            "GUI slot helper must update the real slot hitbox coordinates after 1.21 final-field changes; x="
                + slot.x + ", y=" + slot.y);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void customHeadsAreValidSkullBlockEntities(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (TinkerHeadType type : TinkerHeadType.values()) {
            Block head = TinkerWorld.heads.get(type);
            BlockPos headPos = helper.absolutePos(new BlockPos(1 + type.ordinal() % 6, 2, 1 + type.ordinal() / 6));
            level.setBlock(headPos, head.defaultBlockState(), Block.UPDATE_ALL);
            helper.assertTrue(level.getBlockEntity(headPos) != null,
                "Placed TConstruct head must create a skull block entity without crashing; type=" + type + ", state=" + level.getBlockState(headPos));
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void smelteryFuelUiKeepsLastTankAfterDataSlotSync(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos controllerPos = helper.absolutePos(new BlockPos(2, 2, 1));
        BlockPos fuelTankPos = controllerPos.offset(1, 0, 1);

        buildMinimalHeatingStructure(level, controllerPos, true);
        SmelteryBlockEntity smeltery = (SmelteryBlockEntity) level.getBlockEntity(controllerPos);
        helper.assertTrue(smeltery != null, "Smeltery controller must create a block entity for fuel DataSlot sync regression");
        smeltery.updateStructure();

        helper.runAfterDelay(2, () -> {
            assertRealHeatingStructure(helper, smeltery, controllerPos, "Smeltery fuel DataSlot sync regression");

            IFluidHandler fuelTank = level.getCapability(Capabilities.FluidHandler.BLOCK, fuelTankPos, level.getBlockState(fuelTankPos), level.getBlockEntity(fuelTankPos), null);
            helper.assertTrue(fuelTank != null, "Fuel tank must expose a fluid handler before DataSlot sync regression");
            helper.assertTrue(fuelTank.fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE) == 1000,
                "Fuel tank must accept one bucket of lava before DataSlot sync regression");

            MultitankFuelModule fuelModule = smeltery.getFuelModule();
            helper.assertTrue(fuelModule.findFuel(true) >= 1000, "Fuel module must consume lava and remember the active fuel tank");
            fuelModule.set(4, fuelTankPos.getX());
            fuelModule.set(5, fuelTankPos.getY());
            fuelModule.set(6, fuelTankPos.getZ());

            helper.assertTrue(fuelModule.get(4) == fuelTankPos.getX() && fuelModule.get(5) == fuelTankPos.getY() && fuelModule.get(6) == fuelTankPos.getZ(),
                "Client fuel DataSlot sync must preserve the last fuel tank position instead of resetting it; got="
                    + fuelModule.get(4) + "," + fuelModule.get(5) + "," + fuelModule.get(6)
                    + " expected=" + fuelTankPos.getX() + "," + fuelTankPos.getY() + "," + fuelTankPos.getZ());
            helper.assertTrue(!fuelModule.getFuelInfos().isEmpty(), "Fuel UI must still be able to resolve fuel info after last tank DataSlot sync");
            fuelModule.clearFluidListeners();
            helper.assertTrue(fuelModule.get(4) == fuelTankPos.getX() && fuelModule.get(5) == fuelTankPos.getY() && fuelModule.get(6) == fuelTankPos.getZ(),
                "Structure rebuild must invalidate cached handlers without forgetting the last fuel tank position; got="
                    + fuelModule.get(4) + "," + fuelModule.get(5) + "," + fuelModule.get(6)
                    + " expected=" + fuelTankPos.getX() + "," + fuelTankPos.getY() + "," + fuelTankPos.getZ());
            helper.assertTrue(!fuelModule.getFuelInfos().isEmpty(), "Fuel UI must still resolve fuel info after a structure rebuild cache clear");

            java.util.concurrent.atomic.AtomicReference<List<BlockPos>> transientTankPositions = new java.util.concurrent.atomic.AtomicReference<>(List.of(fuelTankPos));
            MultitankFuelModule transientFuelModule = new MultitankFuelModule(smeltery, transientTankPositions::get);
            helper.assertTrue(transientFuelModule.getTanks() == 1, "Transient fuel module must initially report one known fuel tank");
            transientTankPositions.set(List.of());
            transientFuelModule.clearFluidListeners();
            helper.assertTrue(transientFuelModule.getTanks() == 1,
                "Fuel GUI tank count must stay stable during a transient client-side structure resync instead of reporting no tank");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void smelteryFuelLookupSurvivesQueuedClientReload(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos controllerPos = helper.absolutePos(new BlockPos(2, 2, 1));
        BlockPos fuelTankPos = controllerPos.offset(1, 0, 1);

        buildMinimalHeatingStructure(level, controllerPos, true);
        SmelteryBlockEntity smeltery = (SmelteryBlockEntity) level.getBlockEntity(controllerPos);
        helper.assertTrue(smeltery != null, "Smeltery controller must create a block entity for queued fuel reload regression");
        smeltery.updateStructure();

        helper.runAfterDelay(2, () -> {
            assertRealHeatingStructure(helper, smeltery, controllerPos, "Smeltery queued fuel reload regression");

            IFluidHandler fuelTank = level.getCapability(Capabilities.FluidHandler.BLOCK, fuelTankPos, level.getBlockState(fuelTankPos), level.getBlockEntity(fuelTankPos), null);
            helper.assertTrue(fuelTank != null, "Fuel tank must expose a fluid handler before filling lava");
            int filled = fuelTank.fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE);
            helper.assertTrue(filled == 1000, "Fuel tank must accept one bucket of lava; filled=" + filled);

            RecipeCacheInvalidator.reload(true);
            int possibleTemp = smeltery.getFuelModule().findFuel(false);
            helper.assertTrue(possibleTemp >= 1000,
                "Queued client recipe reload must not crash fallback fuel lookup after filling an active smeltery fuel tank; temp=" + possibleTemp);
            int consumedTemp = smeltery.getFuelModule().findFuel(true);
            helper.assertTrue(consumedTemp >= 1000,
                "Queued client recipe reload must still allow the smeltery to consume lava fuel; temp=" + consumedTemp);
            helper.assertTrue(smeltery.getFuelModule().hasFuel(), "Smeltery must store fuel after consuming lava from the tank");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void fluidItemsExposeAndPersistItemCapabilities(GameTestHelper helper) {
        ItemStack copperCans = new ItemStack(TinkerSmeltery.copperCan.get(), 2);
        IFluidHandlerItem canHandler = copperCans.getCapability(Capabilities.FluidHandler.ITEM);
        helper.assertTrue(canHandler != null, "Copper can stack must expose an item fluid handler");
        helper.assertTrue(canHandler.getTanks() == 1, "Copper can must expose exactly one tank; tanks=" + canHandler.getTanks());
        int canCapacity = FluidValues.INGOT * copperCans.getCount();
        helper.assertTrue(canHandler.getTankCapacity(0) == canCapacity,
            "Copper can capacity must scale by stack size; capacity=" + canHandler.getTankCapacity(0) + ", expected=" + canCapacity);
        helper.assertTrue(canHandler.fill(new FluidStack(Fluids.WATER, canCapacity - 1), FluidAction.EXECUTE) == 0,
            "Copper can must reject partial fills smaller than its full stack capacity");
        helper.assertTrue(CopperCanItem.getFluid(copperCans) == Fluids.EMPTY, "Rejected partial fill must leave copper can empty");

        int simulatedCanFill = canHandler.fill(new FluidStack(Fluids.LAVA, canCapacity), FluidAction.SIMULATE);
        helper.assertTrue(simulatedCanFill == canCapacity, "Copper can simulated full fill must report exact capacity; filled=" + simulatedCanFill);
        helper.assertTrue(CopperCanItem.getFluid(copperCans) == Fluids.EMPTY, "Simulated copper can fill must not write fluid NBT");
        int executedCanFill = canHandler.fill(new FluidStack(Fluids.LAVA, canCapacity), FluidAction.EXECUTE);
        helper.assertTrue(executedCanFill == canCapacity, "Copper can full fill must accept exact capacity; filled=" + executedCanFill);
        helper.assertTrue(CopperCanItem.getFluid(copperCans) == Fluids.LAVA, "Copper can fill must persist lava in item data");
        helper.assertTrue(canHandler.getFluidInTank(0).getFluid() == Fluids.LAVA && canHandler.getFluidInTank(0).getAmount() == canCapacity,
            "Copper can handler must report the persisted lava stack; fluid=" + canHandler.getFluidInTank(0));
        helper.assertTrue(canHandler.fill(new FluidStack(Fluids.WATER, canCapacity), FluidAction.EXECUTE) == 0,
            "Filled copper can must reject replacing lava with another fluid");
        helper.assertTrue(canHandler.drain(canCapacity - 1, FluidAction.EXECUTE).isEmpty(),
            "Copper can must reject partial drains smaller than its full stack capacity");
        FluidStack simulatedCanDrain = canHandler.drain(new FluidStack(Fluids.LAVA, canCapacity), FluidAction.SIMULATE);
        helper.assertTrue(simulatedCanDrain.getFluid() == Fluids.LAVA && simulatedCanDrain.getAmount() == canCapacity,
            "Copper can simulated drain must return the full persisted stack; drained=" + simulatedCanDrain);
        helper.assertTrue(CopperCanItem.getFluid(copperCans) == Fluids.LAVA, "Simulated copper can drain must not clear fluid NBT");
        FluidStack executedCanDrain = canHandler.drain(new FluidStack(Fluids.LAVA, canCapacity), FluidAction.EXECUTE);
        helper.assertTrue(executedCanDrain.getFluid() == Fluids.LAVA && executedCanDrain.getAmount() == canCapacity,
            "Copper can full drain must return the full lava stack; drained=" + executedCanDrain);
        helper.assertTrue(CopperCanItem.getFluid(copperCans) == Fluids.EMPTY, "Executed copper can drain must clear fluid NBT");

        ItemStack tanks = new ItemStack(TinkerSmeltery.searedTank.get(TankType.FUEL_TANK), 2);
        IFluidHandlerItem tankHandler = tanks.getCapability(Capabilities.FluidHandler.ITEM);
        helper.assertTrue(tankHandler != null, "Seared fuel tank item stack must expose an item fluid handler");
        int tankCapacity = TankBlockEntity.getCapacity(tanks.getItem()) * tanks.getCount();
        helper.assertTrue(tankCapacity == TankType.FUEL_TANK.getCapacity() * tanks.getCount(),
            "Fuel tank item capacity must match the upstream tank type capacity scaled by stack size; capacity=" + tankCapacity);
        helper.assertTrue(tankHandler.getTankCapacity(0) == tankCapacity,
            "Fuel tank item handler must expose capacity scaled by stack size; capacity=" + tankHandler.getTankCapacity(0) + ", expected=" + tankCapacity);
        int partialTankFill = tankHandler.fill(new FluidStack(Fluids.LAVA, tankCapacity / 2), FluidAction.EXECUTE);
        helper.assertTrue(partialTankFill == tankCapacity / 2, "Tank item must accept partial fills; filled=" + partialTankFill);
        helper.assertTrue(TankItem.getTank(tanks, tanks.getCount()).getFluidAmount() == tankCapacity / 2,
            "Tank item fill must persist partial amount in item NBT; fluid=" + TankItem.getTank(tanks, tanks.getCount()).getFluid());
        int remainingTankFill = tankHandler.fill(new FluidStack(Fluids.LAVA, tankCapacity), FluidAction.EXECUTE);
        helper.assertTrue(remainingTankFill == tankCapacity / 2, "Tank item second fill must cap at remaining capacity; filled=" + remainingTankFill);
        helper.assertTrue(tankHandler.getFluidInTank(0).getAmount() == tankCapacity,
            "Tank item handler must report a full persisted tank after two fills; fluid=" + tankHandler.getFluidInTank(0));
        FluidStack tankDrain = tankHandler.drain(tankCapacity / 4, FluidAction.EXECUTE);
        helper.assertTrue(tankDrain.getFluid() == Fluids.LAVA && tankDrain.getAmount() == tankCapacity / 4,
            "Tank item must support partial draining; drained=" + tankDrain);
        helper.assertTrue(TankItem.getTank(tanks, tanks.getCount()).getFluidAmount() == tankCapacity - (tankCapacity / 4),
            "Tank item partial drain must persist remaining lava in item NBT; fluid=" + TankItem.getTank(tanks, tanks.getCount()).getFluid());
        FluidStack finalTankDrain = tankHandler.drain(tankCapacity, FluidAction.EXECUTE);
        helper.assertTrue(finalTankDrain.getFluid() == Fluids.LAVA && finalTankDrain.getAmount() == tankCapacity - (tankCapacity / 4),
            "Tank item final drain must return all remaining lava; drained=" + finalTankDrain);
        helper.assertTrue(TankItem.getTank(tanks, tanks.getCount()).isEmpty(), "Tank item final drain must clear the persisted tank NBT");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void tankModifierGivesHeldToolFluidCapability(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertTinkerStationRecipeLoaded(helper, level, "tools/modifiers/upgrade/tank", "Upstream tank modifier recipe");

        ItemStack warPick = ToolBuildHandler.buildItemFromMaterials(TinkerTools.warPick.get(), MaterialNBT.builder()
            .add(MaterialIds.iron)
            .add(MaterialIds.wood)
            .add(MaterialIds.string)
            .build());
        helper.assertTrue(warPick.is(TinkerTags.Items.HELD), "War pick must be eligible for held-tool tank modifier recipes");

        ToolStack tool = ToolStack.from(warPick);
        tool.addModifier(ModifierIds.tank, 1);
        helper.assertTrue(tool.getUpgrades().getLevel(ModifierIds.tank) == 1,
            "Tank modifier must be present as an active upgrade; upgrades=" + tool.getUpgrades());
        helper.assertTrue(tool.getModifierList().stream().anyMatch(entry -> entry.getId().equals(TinkerModifiers.tankHandler.getId()) && entry.getLevel() == 1),
            "Tank modifier must grant the fixed tank handler trait; modifiers=" + tool.getModifierList());
        helper.assertTrue(ToolTankHelper.TANK_HELPER.getCapacity(tool) == FluidType.BUCKET_VOLUME,
            "Tank modifier must add one bucket of tool tank capacity per level; capacity=" + ToolTankHelper.TANK_HELPER.getCapacity(tool));

        ItemStack toolStack = tool.createStack();
        IFluidHandlerItem handler = toolStack.getCapability(Capabilities.FluidHandler.ITEM);
        helper.assertTrue(handler != null, "Tank-modified held tool must expose an item fluid handler");
        helper.assertTrue(handler.getTanks() == 1, "Tank-modified held tool must expose exactly one fluid tank; tanks=" + handler.getTanks());
        helper.assertTrue(handler.getTankCapacity(0) == FluidType.BUCKET_VOLUME,
            "Tank-modified held tool fluid handler must expose one bucket of capacity; capacity=" + handler.getTankCapacity(0));

        int simulatedFill = handler.fill(new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME), FluidAction.SIMULATE);
        helper.assertTrue(simulatedFill == FluidType.BUCKET_VOLUME, "Tank-modified held tool must simulate filling one bucket; filled=" + simulatedFill);
        helper.assertTrue(handler.getFluidInTank(0).isEmpty(), "Simulated fill must not persist fluid into the tool");

        int filled = handler.fill(new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME + 250), FluidAction.EXECUTE);
        helper.assertTrue(filled == FluidType.BUCKET_VOLUME, "Tank-modified held tool must cap fill at capacity; filled=" + filled);
        FluidStack contained = handler.getFluidInTank(0);
        helper.assertTrue(contained.getFluid() == Fluids.LAVA && contained.getAmount() == FluidType.BUCKET_VOLUME,
            "Tank-modified held tool must store lava in persistent tool data; fluid=" + contained);

        IFluidHandlerItem refreshedHandler = toolStack.getCapability(Capabilities.FluidHandler.ITEM);
        helper.assertTrue(refreshedHandler != null, "Tank-modified held tool must expose fluid handler after stack refresh");
        FluidStack refreshedFluid = refreshedHandler.getFluidInTank(0);
        helper.assertTrue(refreshedFluid.getFluid() == Fluids.LAVA && refreshedFluid.getAmount() == FluidType.BUCKET_VOLUME,
            "Tank-modified held tool must persist stored lava across capability refresh; fluid=" + refreshedFluid);

        FluidStack drained = refreshedHandler.drain(FluidType.BUCKET_VOLUME / 2, FluidAction.EXECUTE);
        helper.assertTrue(drained.getFluid() == Fluids.LAVA && drained.getAmount() == FluidType.BUCKET_VOLUME / 2,
            "Tank-modified held tool must support partial draining; drained=" + drained);
        helper.assertTrue(refreshedHandler.getFluidInTank(0).getAmount() == FluidType.BUCKET_VOLUME / 2,
            "Tank-modified held tool must persist partial drain amount; fluid=" + refreshedHandler.getFluidInTank(0));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void fluidContainerTransfersFillAndEmptyFoodContainers(GameTestHelper helper) {
        FluidContainerTransferManager transfers = FluidContainerTransferManager.INSTANCE;
        helper.assertTrue(transfers.mayHaveTransfer(Items.GLASS_BOTTLE), "Fluid transfer manager must know glass bottles can be filled from TConstruct fluids");
        helper.assertTrue(transfers.mayHaveTransfer(Items.BOWL), "Fluid transfer manager must know bowls can be filled from TConstruct fluids");
        helper.assertTrue(transfers.mayHaveTransfer(TinkerFluids.meatSoupBowl.get()), "Fluid transfer manager must know meat soup bowls can be emptied");

        FluidTank venomTank = new FluidTank(FluidValues.BOTTLE * 2);
        int venomFilled = venomTank.fill(new FluidStack(TinkerFluids.venom.get(), FluidValues.BOTTLE * 2), FluidAction.EXECUTE);
        helper.assertTrue(venomFilled == FluidValues.BOTTLE * 2, "Test venom tank must accept two bottles of venom; filled=" + venomFilled);
        FluidStack availableVenom = venomTank.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
        IFluidContainerTransfer venomBottleTransfer = transfers.getTransfer(new ItemStack(Items.GLASS_BOTTLE), availableVenom);
        helper.assertTrue(venomBottleTransfer != null, "Glass bottle + venom tank fluid must resolve the TConstruct venom bottle fill transfer");
        TransferResult venomBottle = venomBottleTransfer.transfer(new ItemStack(Items.GLASS_BOTTLE), availableVenom, venomTank, TransferDirection.AUTO);
        helper.assertTrue(venomBottle != null, "Venom bottle transfer must execute");
        helper.assertTrue(venomBottle.didFill(), "Venom transfer must fill the item from the tank");
        helper.assertTrue(venomBottle.stack().is(TinkerFluids.venomBottle.get()), "Venom transfer must return a venom bottle; stack=" + venomBottle.stack());
        helper.assertTrue(venomBottle.fluid().getFluid() == TinkerFluids.venom.get() && venomBottle.fluid().getAmount() == FluidValues.BOTTLE,
            "Venom transfer must move exactly one bottle of venom; fluid=" + venomBottle.fluid());
        helper.assertTrue(venomTank.getFluidAmount() == FluidValues.BOTTLE,
            "Venom transfer must drain exactly one bottle from the tank; remaining=" + venomTank.getFluid());
        helper.assertTrue(venomBottleTransfer.transfer(new ItemStack(Items.GLASS_BOTTLE), venomTank.getFluid(), venomTank, TransferDirection.EMPTY_ITEM) == null,
            "Fill-only venom bottle transfer must not run when only empty-item direction is allowed");

        FluidTank soupFillTank = new FluidTank(FluidValues.BOWL);
        int soupFilled = soupFillTank.fill(new FluidStack(TinkerFluids.meatSoup.get(), FluidValues.BOWL), FluidAction.EXECUTE);
        helper.assertTrue(soupFilled == FluidValues.BOWL, "Test meat soup tank must accept one bowl of soup; filled=" + soupFilled);
        FluidStack availableSoup = soupFillTank.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
        IFluidContainerTransfer soupFillTransfer = transfers.getTransfer(new ItemStack(Items.BOWL), availableSoup);
        helper.assertTrue(soupFillTransfer != null, "Bowl + meat soup tank fluid must resolve the TConstruct meat soup fill transfer");
        TransferResult filledSoupBowl = soupFillTransfer.transfer(new ItemStack(Items.BOWL), availableSoup, soupFillTank, TransferDirection.AUTO);
        helper.assertTrue(filledSoupBowl != null, "Meat soup bowl fill transfer must execute");
        helper.assertTrue(filledSoupBowl.didFill(), "Meat soup fill transfer must fill the item from the tank");
        helper.assertTrue(filledSoupBowl.stack().is(TinkerFluids.meatSoupBowl.get()), "Meat soup fill transfer must return the TConstruct meat soup item; stack=" + filledSoupBowl.stack());
        helper.assertTrue(soupFillTank.isEmpty(), "Meat soup fill transfer must drain the source tank; remaining=" + soupFillTank.getFluid());

        FluidTank soupEmptyTank = new FluidTank(FluidValues.BOWL);
        IFluidContainerTransfer soupEmptyTransfer = transfers.getTransfer(new ItemStack(TinkerFluids.meatSoupBowl.get()), FluidStack.EMPTY);
        helper.assertTrue(soupEmptyTransfer != null, "Meat soup bowl must resolve the TConstruct empty transfer");
        TransferResult emptiedSoupBowl = soupEmptyTransfer.transfer(new ItemStack(TinkerFluids.meatSoupBowl.get()), FluidStack.EMPTY, soupEmptyTank, TransferDirection.AUTO);
        helper.assertTrue(emptiedSoupBowl != null, "Meat soup bowl empty transfer must execute");
        helper.assertTrue(!emptiedSoupBowl.didFill(), "Meat soup empty transfer must drain the item into the tank");
        helper.assertTrue(emptiedSoupBowl.stack().is(Items.BOWL), "Meat soup empty transfer must return a vanilla bowl; stack=" + emptiedSoupBowl.stack());
        helper.assertTrue(soupEmptyTank.getFluid().getFluid() == TinkerFluids.meatSoup.get() && soupEmptyTank.getFluidAmount() == FluidValues.BOWL,
            "Meat soup empty transfer must fill the tank with exactly one bowl of soup; tank=" + soupEmptyTank.getFluid());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void tankBlockBucketClicksFillAndDrainFluid(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockPos tankPos = helper.absolutePos(relativePos);

        level.setBlock(tankPos, TinkerSmeltery.searedTank.get(TankType.FUEL_TANK).defaultBlockState(), Block.UPDATE_ALL);
        TankBlockEntity tank = (TankBlockEntity) level.getBlockEntity(tankPos);
        helper.assertTrue(tank != null, "Seared fuel tank block must create a tank block entity");
        helper.assertTrue(tank.getTank().isEmpty(), "Newly placed seared fuel tank must start empty");

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LAVA_BUCKET));
        helper.useBlock(relativePos, player);

        FluidStack filledFluid = tank.getTank().getFluid();
        helper.assertTrue(filledFluid.getFluid() == Fluids.LAVA && filledFluid.getAmount() == 1000,
            "Clicking a seared tank with a lava bucket must fill exactly one bucket; fluid=" + filledFluid);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BUCKET),
            "Lava bucket click must leave an empty bucket in the player's hand; hand=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        helper.assertTrue(level.getBlockState(tankPos).getValue(SearedTankBlock.LIGHT) > 0,
            "Filled lava tank block must update its light level; state=" + level.getBlockState(tankPos));

        helper.useBlock(relativePos, player);

        helper.assertTrue(tank.getTank().isEmpty(), "Clicking a filled seared tank with an empty bucket must drain the tank; fluid=" + tank.getTank().getFluid());
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.LAVA_BUCKET),
            "Empty bucket click must return a lava bucket in the player's hand; hand=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        helper.assertTrue(level.getBlockState(tankPos).getValue(SearedTankBlock.LIGHT) == 0,
            "Drained lava tank block must clear its light level; state=" + level.getBlockState(tankPos));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void fluidCannonShootsLoadedFluidEffectProjectile(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockState state = TinkerSmeltery.searedFluidCannon.get().defaultBlockState()
            .setValue(DirectionalBlock.FACING, Direction.EAST);
        level.setBlock(pos, state, Block.UPDATE_ALL);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        helper.assertTrue(blockEntity instanceof FluidCannonBlockEntity,
            "Seared fluid cannon must create a fluid cannon block entity; current=" + (blockEntity == null ? "null" : blockEntity.getClass().getName()));
        FluidCannonBlockEntity cannon = (FluidCannonBlockEntity)blockEntity;

        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, cannon, null);
        helper.assertTrue(itemHandler != null, "Fluid cannon must expose an item handler capability for projectile context");
        ItemStack insertRemainder = itemHandler.insertItem(0, new ItemStack(Items.IRON_INGOT), false);
        helper.assertTrue(insertRemainder.isEmpty(), "Fluid cannon item handler capability must accept a context stack; remainder=" + insertRemainder);
        helper.assertTrue(itemHandler.getStackInSlot(0).is(Items.IRON_INGOT),
            "Fluid cannon item handler capability must expose the stored stack; stack=" + itemHandler.getStackInSlot(0));

        FluidEffects lavaEffects = FluidEffectManager.INSTANCE.find(Fluids.LAVA);
        helper.assertTrue(lavaEffects.hasEffects(), "Lava fluid effect JSON must be loaded for fluid cannon shots");
        helper.assertTrue(lavaEffects.getAmount(Fluids.LAVA) == 50,
            "Lava fluid effect amount must match upstream cannon/spitting cost; amount=" + lavaEffects.getAmount(Fluids.LAVA));

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        FluidStack effectFluid = new FluidStack(Fluids.LAVA, lavaEffects.getAmount(Fluids.LAVA));
        int entityUsed = lavaEffects.applyToEntity(effectFluid, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.EXECUTE);
        helper.assertTrue(entityUsed == lavaEffects.getAmount(Fluids.LAVA),
            "Lava entity fluid effect must consume one effect unit; used=" + entityUsed + ", amount=" + lavaEffects.getAmount(Fluids.LAVA));
        helper.assertTrue(player.getRemainingFireTicks() > 0,
            "Lava entity fluid effect must set the target on fire; fireTicks=" + player.getRemainingFireTicks());

        BlockPos supportPos = pos.offset(2, 0, 0);
        BlockPos firePos = supportPos.above();
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(firePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        BlockHitResult lavaHit = new BlockHitResult(Vec3.atCenterOf(supportPos), Direction.UP, supportPos, false);
        int blockUsed = lavaEffects.applyToBlock(effectFluid, 1, FluidEffectContext.builder(level).user(player).block(lavaHit), FluidAction.EXECUTE);
        helper.assertTrue(blockUsed == lavaEffects.getAmount(Fluids.LAVA),
            "Lava block fluid effect must consume one effect unit; used=" + blockUsed + ", amount=" + lavaEffects.getAmount(Fluids.LAVA));
        helper.assertTrue(level.getBlockState(firePos).is(Blocks.FIRE),
            "Lava block fluid effect must place fire above the clicked support block; state=" + level.getBlockState(firePos));

        int initialAmount = 500;
        int expectedShotAmount = Math.min(initialAmount, (int)(lavaEffects.getAmount(Fluids.LAVA) * TinkerSmeltery.searedFluidCannon.get().getPower()));
        int filled = cannon.getTank().fill(new FluidStack(Fluids.LAVA, initialAmount), FluidAction.EXECUTE);
        helper.assertTrue(filled == initialAmount, "Fluid cannon tank must accept lava before shooting; filled=" + filled);

        cannon.shoot(state, level, RandomSource.create(1234L));
        FluidStack remaining = cannon.getTank().getFluid();
        helper.assertTrue(remaining.getFluid() == Fluids.LAVA && remaining.getAmount() == initialAmount - expectedShotAmount,
            "Fluid cannon shot must consume exactly one lava fluid effect amount; remaining=" + remaining + ", expected=" + (initialAmount - expectedShotAmount));

        List<FluidEffectProjectile> projectiles = level.getEntitiesOfClass(FluidEffectProjectile.class, new AABB(pos).inflate(4));
        helper.assertTrue(projectiles.size() == 1, "Fluid cannon shot must spawn exactly one fluid effect projectile; count=" + projectiles.size());
        FluidEffectProjectile projectile = projectiles.getFirst();
        helper.assertTrue(projectile.getType() == TinkerModifiers.fluidSpitEntity.get(),
            "Fluid cannon projectile must use the registered tconstruct fluid spit entity type; current=" + projectile.getType());
        helper.assertTrue(projectile.getFluid().getFluid() == Fluids.LAVA && projectile.getFluid().getAmount() == expectedShotAmount,
            "Fluid cannon projectile must carry the consumed lava amount; fluid=" + projectile.getFluid() + ", expected=" + expectedShotAmount);
        helper.assertTrue(projectile.getPower() == TinkerSmeltery.searedFluidCannon.get().getPower(),
            "Fluid cannon projectile must inherit the cannon power; power=" + projectile.getPower());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void venomFluidEffectAppliesUpstreamMobEffects(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);

        FluidEffects venomEffects = FluidEffectManager.INSTANCE.find(TinkerFluids.venom.get());
        helper.assertTrue(venomEffects.hasEntityEffects(), "Venom fluid effect JSON must expose entity effects");
        helper.assertTrue(venomEffects.getAmount(TinkerFluids.venom.get()) == FluidValues.SIP,
            "Venom fluid effect must use the upstream sip cost; amount=" + venomEffects.getAmount(TinkerFluids.venom.get()));

        FluidStack venom = new FluidStack(TinkerFluids.venom.get(), venomEffects.getAmount(TinkerFluids.venom.get()));
        int used = venomEffects.applyToEntity(venom, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.EXECUTE);
        helper.assertTrue(used == FluidValues.SIP, "Venom entity effect must consume one sip; used=" + used + ", amount=" + FluidValues.SIP);
        helper.assertTrue(player.hasEffect(MobEffects.POISON),
            "Venom entity effect must apply poison from upstream fluid_effects/venom.json");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST),
            "Venom entity effect must apply strength from upstream fluid_effects/venom.json");
        helper.assertTrue(player.getEffect(MobEffects.POISON).getDuration() >= 90,
            "Venom poison duration must be close to the upstream 100 ticks; effect=" + player.getEffect(MobEffects.POISON));
        helper.assertTrue(player.getEffect(MobEffects.DAMAGE_BOOST).getDuration() >= 190,
            "Venom strength duration must be close to the upstream 200 ticks; effect=" + player.getEffect(MobEffects.DAMAGE_BOOST));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void meatSoupFluidEffectRestoresFoodFromUpstreamJson(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.getFoodData().setFoodLevel(10);
        player.getFoodData().setSaturation(0);

        FluidEffects soupEffects = FluidEffectManager.INSTANCE.find(TinkerFluids.meatSoup.get());
        helper.assertTrue(soupEffects.hasEntityEffects(), "Meat soup fluid effect JSON must expose entity effects");
        helper.assertTrue(soupEffects.getAmount(TinkerFluids.meatSoup.get()) == FluidValues.SIP,
            "Meat soup fluid effect must use the upstream sip cost; amount=" + soupEffects.getAmount(TinkerFluids.meatSoup.get()));

        FluidStack soup = new FluidStack(TinkerFluids.meatSoup.get(), soupEffects.getAmount(TinkerFluids.meatSoup.get()));
        int simulated = soupEffects.applyToEntity(soup, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.SIMULATE);
        helper.assertTrue(simulated == FluidValues.SIP, "Meat soup simulated entity effect must report one sip consumed; used=" + simulated);
        helper.assertTrue(player.getFoodData().getFoodLevel() == 10,
            "Meat soup simulated entity effect must not change hunger; food=" + player.getFoodData().getFoodLevel());
        helper.assertTrue(player.getFoodData().getSaturationLevel() == 0,
            "Meat soup simulated entity effect must not change saturation; saturation=" + player.getFoodData().getSaturationLevel());

        int used = soupEffects.applyToEntity(soup, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.EXECUTE);
        helper.assertTrue(used == FluidValues.SIP, "Meat soup entity effect must consume one sip; used=" + used + ", amount=" + FluidValues.SIP);
        helper.assertTrue(player.getFoodData().getFoodLevel() == 12,
            "Meat soup entity effect must restore the upstream 2 hunger points; food=" + player.getFoodData().getFoodLevel());
        helper.assertTrue(player.getFoodData().getSaturationLevel() >= 1.9F,
            "Meat soup entity effect must restore saturation from upstream 0.48 modifier; saturation=" + player.getFoodData().getSaturationLevel());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void moltenCopperFluidEffectDamagesAndRestoresBreath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.setAirSupply(100);
        float beforeHealth = player.getHealth();

        FluidEffects copperEffects = FluidEffectManager.INSTANCE.find(TinkerFluids.moltenCopper.get());
        helper.assertTrue(copperEffects.hasEntityEffects(), "Molten copper fluid effect JSON must expose entity effects");
        helper.assertTrue(copperEffects.getAmount(TinkerFluids.moltenCopper.get()) == FluidValues.NUGGET,
            "Molten copper fluid effect must use the upstream nugget cost; amount=" + copperEffects.getAmount(TinkerFluids.moltenCopper.get()));

        FluidStack copper = new FluidStack(TinkerFluids.moltenCopper.get(), copperEffects.getAmount(TinkerFluids.moltenCopper.get()));
        int simulated = copperEffects.applyToEntity(copper, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.SIMULATE);
        helper.assertTrue(simulated == FluidValues.NUGGET, "Molten copper simulated entity effect must report one nugget consumed; used=" + simulated);
        helper.assertTrue(player.getAirSupply() == 100,
            "Molten copper simulated entity effect must not change air supply; air=" + player.getAirSupply());
        helper.assertTrue(player.getHealth() == beforeHealth,
            "Molten copper simulated entity effect must not damage the player; health=" + player.getHealth());

        int used = copperEffects.applyToEntity(copper, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.EXECUTE);
        helper.assertTrue(used == FluidValues.NUGGET, "Molten copper entity effect must consume one nugget; used=" + used + ", amount=" + FluidValues.NUGGET);
        helper.assertTrue(player.getAirSupply() == 190,
            "Molten copper entity effect must restore the upstream 90 ticks of breath; air=" + player.getAirSupply());
        helper.assertTrue(player.getHealth() < beforeHealth,
            "Molten copper entity effect must apply upstream fluid fire damage; before=" + beforeHealth + ", after=" + player.getHealth());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void moltenGoldFluidEffectAppliesMagicDamageRegenAndCloud(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        float beforeHealth = player.getHealth();

        FluidEffects goldEffects = FluidEffectManager.INSTANCE.find(TinkerFluids.moltenGold.get());
        helper.assertTrue(goldEffects.hasEntityEffects(), "Molten gold fluid effect JSON must expose entity effects");
        helper.assertTrue(goldEffects.hasBlockEffects(), "Molten gold fluid effect JSON must expose block cloud effects");
        helper.assertTrue(goldEffects.getAmount(TinkerFluids.moltenGold.get()) == FluidValues.NUGGET,
            "Molten gold fluid effect must use the upstream nugget cost; amount=" + goldEffects.getAmount(TinkerFluids.moltenGold.get()));

        FluidStack gold = new FluidStack(TinkerFluids.moltenGold.get(), goldEffects.getAmount(TinkerFluids.moltenGold.get()));
        int entitySimulated = goldEffects.applyToEntity(gold, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.SIMULATE);
        helper.assertTrue(entitySimulated == FluidValues.NUGGET,
            "Molten gold simulated entity effect must report one nugget consumed; used=" + entitySimulated);
        helper.assertTrue(player.getHealth() == beforeHealth,
            "Molten gold simulated entity effect must not damage the player; health=" + player.getHealth());
        helper.assertTrue(!player.hasEffect(MobEffects.REGENERATION),
            "Molten gold simulated entity effect must not apply regeneration");

        int entityUsed = goldEffects.applyToEntity(gold, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.EXECUTE);
        helper.assertTrue(entityUsed == FluidValues.NUGGET, "Molten gold entity effect must consume one nugget; used=" + entityUsed);
        helper.assertTrue(player.getHealth() < beforeHealth,
            "Molten gold entity effect must apply upstream magic damage; before=" + beforeHealth + ", after=" + player.getHealth());
        helper.assertTrue(player.hasEffect(MobEffects.REGENERATION),
            "Molten gold entity effect must apply upstream regeneration");
        helper.assertTrue(player.getEffect(MobEffects.REGENERATION).getDuration() >= 110,
            "Molten gold regeneration duration must be close to upstream 120 ticks; effect=" + player.getEffect(MobEffects.REGENERATION));

        BlockPos supportPos = helper.absolutePos(new BlockPos(4, 1, 1));
        BlockPos cloudPos = supportPos.above();
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(cloudPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(supportPos), Direction.UP, supportPos, false);

        int cloudsBefore = level.getEntitiesOfClass(AreaEffectCloud.class, new AABB(cloudPos).inflate(2)).size();
        int blockUsed = goldEffects.applyToBlock(gold, 1, FluidEffectContext.builder(level).user(player).block(hit), FluidAction.EXECUTE);
        List<AreaEffectCloud> clouds = level.getEntitiesOfClass(AreaEffectCloud.class, new AABB(cloudPos).inflate(2));
        helper.assertTrue(blockUsed == FluidValues.NUGGET, "Molten gold block cloud effect must consume one nugget; used=" + blockUsed);
        helper.assertTrue(clouds.size() == cloudsBefore + 1,
            "Molten gold block effect must spawn a regeneration area effect cloud; before=" + cloudsBefore + ", after=" + clouds.size());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void blazingBloodFluidEffectBurnsGlowsAndPlacesGlowBlock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        float beforeHealth = player.getHealth();
        int beforeFireTicks = player.getRemainingFireTicks();

        FluidEffects blazingBloodEffects = FluidEffectManager.INSTANCE.find(TinkerFluids.blazingBlood.get());
        helper.assertTrue(blazingBloodEffects.hasEntityEffects(), "Blazing blood fluid effect JSON must expose entity effects");
        helper.assertTrue(blazingBloodEffects.hasBlockEffects(), "Blazing blood fluid effect JSON must expose block effects");
        helper.assertTrue(blazingBloodEffects.getAmount(TinkerFluids.blazingBlood.get()) == FluidValues.SIP,
            "Blazing blood fluid effect must use the upstream sip cost; amount=" + blazingBloodEffects.getAmount(TinkerFluids.blazingBlood.get()));

        FluidStack blazingBlood = new FluidStack(TinkerFluids.blazingBlood.get(), blazingBloodEffects.getAmount(TinkerFluids.blazingBlood.get()));
        int entitySimulated = blazingBloodEffects.applyToEntity(blazingBlood, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.SIMULATE);
        helper.assertTrue(entitySimulated == FluidValues.SIP,
            "Blazing blood simulated entity effect must report one sip consumed; used=" + entitySimulated);
        helper.assertTrue(player.getHealth() == beforeHealth,
            "Blazing blood simulated entity effect must not damage the player; health=" + player.getHealth());
        helper.assertTrue(player.getRemainingFireTicks() == beforeFireTicks,
            "Blazing blood simulated entity effect must not change fire ticks; before=" + beforeFireTicks + ", after=" + player.getRemainingFireTicks());
        helper.assertTrue(!player.hasEffect(MobEffects.GLOWING),
            "Blazing blood simulated entity effect must not apply glowing");

        int entityUsed = blazingBloodEffects.applyToEntity(blazingBlood, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.EXECUTE);
        helper.assertTrue(entityUsed == FluidValues.SIP, "Blazing blood entity effect must consume one sip; used=" + entityUsed);
        helper.assertTrue(player.getHealth() < beforeHealth,
            "Blazing blood entity effect must apply upstream fire damage; before=" + beforeHealth + ", after=" + player.getHealth());
        helper.assertTrue(player.getRemainingFireTicks() > beforeFireTicks,
            "Blazing blood entity effect must increase fire ticks; before=" + beforeFireTicks + ", after=" + player.getRemainingFireTicks());
        helper.assertTrue(player.hasEffect(MobEffects.GLOWING),
            "Blazing blood entity effect must apply upstream glowing");
        helper.assertTrue(player.getEffect(MobEffects.GLOWING).getDuration() >= 90,
            "Blazing blood glowing duration must be close to upstream 100 ticks; effect=" + player.getEffect(MobEffects.GLOWING));

        BlockPos supportPos = helper.absolutePos(new BlockPos(5, 1, 1));
        BlockPos glowPos = supportPos.above();
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(glowPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(supportPos), Direction.UP, supportPos, false);

        int blockUsed = blazingBloodEffects.applyToBlock(blazingBlood, 1, FluidEffectContext.builder(level).user(player).block(hit), FluidAction.EXECUTE);
        helper.assertTrue(blockUsed == FluidValues.SIP, "Blazing blood block effect must consume one sip; used=" + blockUsed);
        helper.assertTrue(level.getBlockState(glowPos).is(TinkerCommons.glowBlock.get()),
            "Blazing blood block effect must place the registered glow block; state=" + level.getBlockState(glowPos));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void powderedSnowFluidEffectFreezesAndPlacesSnow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        helper.assertTrue(player.canFreeze(), "Mock player must be eligible for powdered snow's can_freeze conditional damage");
        player.setRemainingFireTicks(80);
        int beforeFireTicks = player.getRemainingFireTicks();
        int beforeFrozenTicks = player.getTicksFrozen();
        int requiredFreezeTicks = player.getTicksRequiredToFreeze();
        float beforeHealth = player.getHealth();

        FluidEffects powderedSnowEffects = FluidEffectManager.INSTANCE.find(TinkerFluids.powderedSnow.get());
        helper.assertTrue(powderedSnowEffects.hasEntityEffects(), "Powdered snow fluid effect JSON must expose entity effects");
        helper.assertTrue(powderedSnowEffects.hasBlockEffects(), "Powdered snow fluid effect JSON must expose block effects");
        helper.assertTrue(powderedSnowEffects.getAmount(TinkerFluids.powderedSnow.get()) == FluidType.BUCKET_VOLUME / 10,
            "Powdered snow fluid effect must use the upstream 100 mB cost; amount=" + powderedSnowEffects.getAmount(TinkerFluids.powderedSnow.get()));

        FluidStack powderedSnow = new FluidStack(TinkerFluids.powderedSnow.get(), powderedSnowEffects.getAmount(TinkerFluids.powderedSnow.get()));
        int entitySimulated = powderedSnowEffects.applyToEntity(powderedSnow, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.SIMULATE);
        helper.assertTrue(entitySimulated == FluidType.BUCKET_VOLUME / 10,
            "Powdered snow simulated entity effect must report the upstream 100 mB consumed; used=" + entitySimulated);
        helper.assertTrue(player.getHealth() == beforeHealth,
            "Powdered snow simulated entity effect must not damage the player; health=" + player.getHealth());
        helper.assertTrue(player.getTicksFrozen() == beforeFrozenTicks,
            "Powdered snow simulated entity effect must not change frozen ticks; before=" + beforeFrozenTicks + ", after=" + player.getTicksFrozen());
        helper.assertTrue(player.getRemainingFireTicks() == beforeFireTicks,
            "Powdered snow simulated entity effect must not clear fire ticks; before=" + beforeFireTicks + ", after=" + player.getRemainingFireTicks());

        int entityUsed = powderedSnowEffects.applyToEntity(powderedSnow, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.EXECUTE);
        helper.assertTrue(entityUsed == FluidType.BUCKET_VOLUME / 10, "Powdered snow entity effect must consume 100 mB; used=" + entityUsed);
        helper.assertTrue(player.getHealth() < beforeHealth,
            "Powdered snow entity effect must apply upstream cold damage; before=" + beforeHealth + ", after=" + player.getHealth());
        helper.assertTrue(player.getTicksFrozen() >= requiredFreezeTicks + 70,
            "Powdered snow entity effect must add enough frozen ticks to freeze the target; required=" + requiredFreezeTicks + ", frozen=" + player.getTicksFrozen());
        helper.assertTrue(player.getRemainingFireTicks() <= 0,
            "Powdered snow freeze effect must clear fire ticks; fireTicks=" + player.getRemainingFireTicks());

        BlockPos supportPos = helper.absolutePos(new BlockPos(6, 1, 1));
        BlockPos snowPos = supportPos.above();
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(snowPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(supportPos), Direction.UP, supportPos, false);

        int blockSimulated = powderedSnowEffects.applyToBlock(powderedSnow, 1, FluidEffectContext.builder(level).user(player).block(hit), FluidAction.SIMULATE);
        helper.assertTrue(blockSimulated == FluidType.BUCKET_VOLUME / 10,
            "Powdered snow simulated block effect must report the upstream 100 mB consumed; used=" + blockSimulated);
        helper.assertTrue(level.getBlockState(snowPos).isAir(),
            "Powdered snow simulated block effect must not place snow; state=" + level.getBlockState(snowPos));

        int blockUsed = powderedSnowEffects.applyToBlock(powderedSnow, 1, FluidEffectContext.builder(level).user(player).block(hit), FluidAction.EXECUTE);
        helper.assertTrue(blockUsed == FluidType.BUCKET_VOLUME / 10, "Powdered snow block effect must consume 100 mB; used=" + blockUsed);
        helper.assertTrue(level.getBlockState(snowPos).is(Blocks.SNOW),
            "Powdered snow block effect must place vanilla snow above the clicked block; state=" + level.getBlockState(snowPos));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void potionFluidEffectReadsPotionDataForEntityAndCloud(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);

        FluidEffects potionEffects = FluidEffectManager.INSTANCE.find(TinkerFluids.potion.get());
        helper.assertTrue(potionEffects.hasEntityEffects(), "Potion fluid effect JSON must expose entity effects");
        helper.assertTrue(potionEffects.hasBlockEffects(), "Potion fluid effect JSON must expose block cloud effects");
        helper.assertTrue(potionEffects.getAmount(TinkerFluids.potion.get()) == FluidValues.SIP,
            "Potion fluid effect must use the upstream sip cost; amount=" + potionEffects.getAmount(TinkerFluids.potion.get()));

        FluidStack swiftness = PotionFluidType.potionFluid(Potions.SWIFTNESS.value(), potionEffects.getAmount(TinkerFluids.potion.get()));
        int entitySimulated = potionEffects.applyToEntity(swiftness, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.SIMULATE);
        helper.assertTrue(entitySimulated == FluidValues.SIP,
            "Potion simulated entity effect must read potion data and report one sip consumed; used=" + entitySimulated);
        helper.assertTrue(!player.hasEffect(MobEffects.MOVEMENT_SPEED),
            "Potion simulated entity effect must not apply swiftness");

        int entityUsed = potionEffects.applyToEntity(swiftness, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.EXECUTE);
        helper.assertTrue(entityUsed == FluidValues.SIP, "Potion entity effect must consume one sip; used=" + entityUsed);
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED),
            "Potion entity effect must apply swiftness from the fluid stack potion data");
        helper.assertTrue(player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration() >= 850,
            "Potion entity effect must scale swiftness duration by upstream 0.25; effect=" + player.getEffect(MobEffects.MOVEMENT_SPEED));

        BlockPos supportPos = helper.absolutePos(new BlockPos(3, 1, 1));
        BlockPos cloudPos = supportPos.above();
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(cloudPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(supportPos), Direction.UP, supportPos, false);

        int cloudsBefore = level.getEntitiesOfClass(AreaEffectCloud.class, new AABB(cloudPos).inflate(2)).size();
        int blockUsed = potionEffects.applyToBlock(swiftness, 1, FluidEffectContext.builder(level).user(player).block(hit), FluidAction.EXECUTE);
        List<AreaEffectCloud> clouds = level.getEntitiesOfClass(AreaEffectCloud.class, new AABB(cloudPos).inflate(2));
        helper.assertTrue(blockUsed == FluidValues.SIP, "Potion block cloud effect must consume one sip; used=" + blockUsed);
        helper.assertTrue(clouds.size() == cloudsBefore + 1,
            "Potion block cloud effect must spawn an area effect cloud from fluid stack potion data; before=" + cloudsBefore + ", after=" + clouds.size());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void skySlimeFluidEffectAppliesSlownessAndPush(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.setDeltaMovement(Vec3.ZERO);

        FluidEffects skySlimeEffects = FluidEffectManager.INSTANCE.find(TinkerFluids.skySlime.get());
        helper.assertTrue(skySlimeEffects.hasEntityEffects(), "Sky slime fluid effect JSON must expose entity effects");
        helper.assertTrue(skySlimeEffects.getAmount(TinkerFluids.skySlime.get()) == FluidValues.SIP,
            "Sky slime fluid effect must use the upstream sip cost; amount=" + skySlimeEffects.getAmount(TinkerFluids.skySlime.get()));

        FluidStack skySlime = new FluidStack(TinkerFluids.skySlime.get(), skySlimeEffects.getAmount(TinkerFluids.skySlime.get()));
        int simulated = skySlimeEffects.applyToEntity(skySlime, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.SIMULATE);
        helper.assertTrue(simulated == FluidValues.SIP, "Sky slime simulated entity effect must report one sip consumed; used=" + simulated);
        helper.assertTrue(!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
            "Sky slime simulated entity effect must not apply slowness");
        helper.assertTrue(player.getDeltaMovement().lengthSqr() == 0,
            "Sky slime simulated entity effect must not push the player; motion=" + player.getDeltaMovement());

        int used = skySlimeEffects.applyToEntity(skySlime, 1, FluidEffectContext.builder(level).user(player).target(player), FluidAction.EXECUTE);
        helper.assertTrue(used == FluidValues.SIP, "Sky slime entity effect must consume one sip; used=" + used);
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
            "Sky slime entity effect must apply upstream slowness");
        helper.assertTrue(player.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getDuration() >= 90,
            "Sky slime slowness duration must be close to upstream 100 ticks; effect=" + player.getEffect(MobEffects.MOVEMENT_SLOWDOWN));
        helper.assertTrue(player.getDeltaMovement().lengthSqr() > 0,
            "Sky slime entity effect must push the target using upstream push_entity; motion=" + player.getDeltaMovement());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void worldSlimeBlocksSeedsAndEntitiesKeepCoreBehavior(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        BlockState skySlime = TinkerWorld.slime.get(SlimeType.SKY).defaultBlockState();
        BlockState ichorSlime = TinkerWorld.slime.get(SlimeType.ICHOR).defaultBlockState();
        BlockState enderSlime = TinkerWorld.slime.get(SlimeType.ENDER).defaultBlockState();
        BlockState vanillaSlime = Blocks.SLIME_BLOCK.defaultBlockState();
        helper.assertTrue(skySlime.isSlimeBlock() && skySlime.isStickyBlock(), "Sky slime block must keep vanilla slime/sticky behavior");
        helper.assertTrue(ichorSlime.isSlimeBlock() && ichorSlime.isStickyBlock(), "Ichor slime block must keep vanilla slime/sticky behavior");
        helper.assertTrue(enderSlime.isSlimeBlock() && enderSlime.isStickyBlock(), "Ender slime block must keep vanilla slime/sticky behavior");
        helper.assertTrue(skySlime.canStickTo(vanillaSlime), "Sky slime must stick to ordinary slime blocks");
        helper.assertTrue(skySlime.canStickTo(ichorSlime), "Sky slime must stick to other slime variants");
        helper.assertTrue(ichorSlime.canStickTo(skySlime), "Ichor slime must stick to other block types");
        helper.assertTrue(!ichorSlime.canStickTo(ichorSlime), "Ichor slime must not stick to itself");
        helper.assertTrue(enderSlime.canStickTo(enderSlime), "Ender slime must stick to itself");
        helper.assertTrue(!enderSlime.canStickTo(skySlime), "Ender slime must not stick to other slime variants");

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        BlockPos grassPos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(grassPos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        ItemStack skySeeds = new ItemStack(TinkerWorld.slimeGrassSeeds.get(FoliageType.SKY), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, skySeeds);
        InteractionResult grassResult = skySeeds.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(grassPos), Direction.UP, grassPos, false)));
        helper.assertTrue(grassResult == InteractionResult.SUCCESS, "Sky slime grass seeds must successfully convert vanilla dirt");
        helper.assertTrue(level.getBlockState(grassPos).is(TinkerWorld.vanillaSlimeGrass.get(FoliageType.SKY)),
            "Sky slime grass seeds on vanilla dirt must create sky vanilla slime grass; state=" + level.getBlockState(grassPos));
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
            "Slime grass seed use must consume one seed outside creative mode; remaining=" + player.getItemInHand(InteractionHand.MAIN_HAND));

        BlockPos vinePos = helper.absolutePos(new BlockPos(3, 1, 1));
        level.setBlock(vinePos, Blocks.VINE.defaultBlockState().setValue(VineBlock.NORTH, true), Block.UPDATE_ALL);
        ItemStack enderSeeds = new ItemStack(TinkerWorld.slimeGrassSeeds.get(FoliageType.ENDER), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, enderSeeds);
        InteractionResult vineResult = enderSeeds.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(vinePos), Direction.NORTH, vinePos, false)));
        BlockState vineState = level.getBlockState(vinePos);
        helper.assertTrue(vineResult == InteractionResult.SUCCESS, "Ender slime grass seeds must successfully convert vanilla vines");
        helper.assertTrue(vineState.is(TinkerWorld.enderSlimeVine.get()), "Ender seeds on vanilla vines must create ender slime vines; state=" + vineState);
        helper.assertTrue(vineState.getValue(SlimeVineBlock.STAGE) == VineStage.START, "Converted slime vine must start in START stage; state=" + vineState);
        helper.assertTrue(vineState.getValue(VineBlock.NORTH), "Converted slime vine must preserve the vanilla vine north face; state=" + vineState);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
            "Slime vine seed use must consume one seed outside creative mode; remaining=" + player.getItemInHand(InteractionHand.MAIN_HAND));

        SkySlimeEntity skyEntity = TinkerWorld.skySlimeEntity.get().create(level);
        EnderSlimeEntity enderEntity = TinkerWorld.enderSlimeEntity.get().create(level);
        TerracubeEntity terracube = TinkerWorld.terracubeEntity.get().create(level);
        helper.assertTrue(skyEntity != null, "Sky slime entity type must create a server entity");
        helper.assertTrue(enderEntity != null, "Ender slime entity type must create a server entity");
        helper.assertTrue(terracube != null, "Terracube entity type must create a server entity");
        helper.assertTrue(skyEntity.getType() == TinkerWorld.skySlimeEntity.get(), "Sky slime entity must use the registered entity type");
        helper.assertTrue(enderEntity.getType() == TinkerWorld.enderSlimeEntity.get(), "Ender slime entity must use the registered entity type");
        helper.assertTrue(terracube.getType() == TinkerWorld.terracubeEntity.get(), "Terracube entity must use the registered entity type");
        helper.assertTrue(skyEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null, "Sky slime must have registered monster attributes");
        helper.assertTrue(enderEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null, "Ender slime must have registered monster attributes");
        helper.assertTrue(terracube.getAttribute(Attributes.ATTACK_DAMAGE) != null, "Terracube must have registered monster attributes");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void slimeFoliageLootUsesNeoForgeShearsAbility(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos vinePos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockState vineState = TinkerWorld.skySlimeVine.get().defaultBlockState()
            .setValue(VineBlock.NORTH, true)
            .setValue(SlimeVineBlock.STAGE, VineStage.START);
        level.setBlock(vinePos, vineState, Block.UPDATE_ALL);

        List<ItemStack> emptyHandDrops = getBlockDrops(level, vineState, vinePos, ItemStack.EMPTY);
        helper.assertTrue(emptyHandDrops.stream().noneMatch(stack -> stack.is(TinkerWorld.skySlimeVine.asItem())),
            "Sky slime vine must not drop itself without the shears_dig item ability; drops=" + emptyHandDrops);

        ItemStack kama = buildIronKama();
        helper.assertTrue(kama.canPerformAction(ItemAbilities.SHEARS_DIG),
            "Built TConstruct kama must expose the SHEARS_DIG item ability through its shears module");
        List<ItemStack> shearsDrops = getBlockDrops(level, vineState, vinePos, kama);
        helper.assertTrue(shearsDrops.stream().anyMatch(stack -> stack.is(TinkerWorld.skySlimeVine.asItem())),
            "Sky slime vine loot table must honor neoforge:can_item_perform_ability shears_dig and drop itself with a shearing TConstruct tool; drops=" + shearsDrops);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void shearsModifierShearsEntitiesThroughNeoForgeShearable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sheepPos = helper.absolutePos(new BlockPos(2, 1, 2));
        Sheep sheep = EntityType.SHEEP.create(level);
        helper.assertTrue(sheep != null, "Vanilla sheep entity must be creatable for shears module regression");
        sheep.setColor(DyeColor.WHITE);
        sheep.setSheared(false);
        sheep.moveTo(sheepPos.getX() + 0.5, sheepPos.getY(), sheepPos.getZ() + 0.5, 0, 0);
        level.addFreshEntity(sheep);
        helper.assertTrue(sheep.readyForShearing(), "Fresh adult unsheared sheep must be ready for shearing before TConstruct interaction");

        ItemStack kama = buildIronKama();
        ToolStack tool = ToolStack.from(kama);
        helper.assertTrue(tool.getModifierLevel(ModifierIds.shears) == 1,
            "Built TConstruct kama must carry the shears modifier from its tool definition; modifiers=" + tool.getModifierList());
        helper.assertTrue(kama.canPerformAction(ItemAbilities.SHEARS_HARVEST),
            "Built TConstruct kama must expose the NeoForge SHEARS_HARVEST item ability for entity shearing");

        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(sheep.getX(), sheep.getY(), sheep.getZ() + 1.0, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, kama);

        int woolBefore = countItems(level, sheep.blockPosition(), Items.WHITE_WOOL);
        InteractionResult result = player.interactOn(sheep, InteractionHand.MAIN_HAND);
        int woolAfter = countItems(level, sheep.blockPosition(), Items.WHITE_WOOL);
        helper.assertTrue(sheep.isSheared(),
            "Shears module must shear sheep through the 1.21 NeoForge IShearable fallback; result=" + result);
        helper.assertTrue(woolAfter > woolBefore,
            "Shears module must spawn wool drops through IShearable.spawnShearedDrop; before=" + woolBefore + ", after=" + woolAfter + ", result=" + result);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void worldgenDatapackEntriesLoad(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        assertRegistryEntry(helper, level, TinkerWorld.configuredEarthGeode, "Configured earth geode");
        assertRegistryEntry(helper, level, TinkerWorld.configuredSkyGeode, "Configured sky geode");
        assertRegistryEntry(helper, level, TinkerWorld.configuredIchorGeode, "Configured ichor geode");
        assertRegistryEntry(helper, level, TinkerWorld.configuredEnderGeode, "Configured ender geode");
        assertRegistryEntry(helper, level, TinkerWorld.configuredSmallCobaltOre, "Configured small cobalt ore");
        assertRegistryEntry(helper, level, TinkerWorld.configuredLargeCobaltOre, "Configured large cobalt ore");
        assertRegistryEntry(helper, level, TinkerStructures.skySlimeTree, "Configured sky slime tree");
        assertRegistryEntry(helper, level, TinkerStructures.earthSlimeTree, "Configured earth slime tree");
        assertRegistryEntry(helper, level, TinkerStructures.enderSlimeTree, "Configured ender slime tree");
        assertRegistryEntry(helper, level, TinkerStructures.bloodSlimeFungus, "Configured blood slime fungus");

        assertRegistryEntry(helper, level, TinkerWorld.placedEarthGeode, "Placed earth geode");
        assertRegistryEntry(helper, level, TinkerWorld.placedSkyGeode, "Placed sky geode");
        assertRegistryEntry(helper, level, TinkerWorld.placedIchorGeode, "Placed ichor geode");
        assertRegistryEntry(helper, level, TinkerWorld.placedEnderGeode, "Placed ender geode");
        assertRegistryEntry(helper, level, TinkerWorld.placedSmallCobaltOre, "Placed small cobalt ore");
        assertRegistryEntry(helper, level, TinkerWorld.placedLargeCobaltOre, "Placed large cobalt ore");

        assertRegistryEntry(helper, level, TinkerStructures.skySlimeIsland, "Sky slime island structure");
        assertRegistryEntry(helper, level, TinkerStructures.earthSlimeIsland, "Earth slime island structure");
        assertRegistryEntry(helper, level, TinkerStructures.oceanSkyslimeIsland, "Ocean skyslime island structure");
        assertRegistryEntry(helper, level, TinkerStructures.clayIsland, "Clay island structure");
        assertRegistryEntry(helper, level, TinkerStructures.bloodIsland, "Blood island structure");
        assertRegistryEntry(helper, level, TinkerStructures.endSlimeIsland, "End slime island structure");

        assertRegistryEntry(helper, level, TinkerStructures.overworldOceanIsland, "Overworld ocean island structure set");
        assertRegistryEntry(helper, level, TinkerStructures.overworldSkyIsland, "Overworld sky island structure set");
        assertRegistryEntry(helper, level, TinkerStructures.netherOceanIsland, "Nether ocean island structure set");
        assertRegistryEntry(helper, level, TinkerStructures.endSkyIsland, "End sky island structure set");

        assertRegistryEntry(helper, level, TinkerWorld.spawnEarthGeode, "Earth geode biome modifier");
        assertRegistryEntry(helper, level, TinkerWorld.spawnSkyGeode, "Sky geode biome modifier");
        assertRegistryEntry(helper, level, TinkerWorld.spawnIchorGeode, "Ichor geode biome modifier");
        assertRegistryEntry(helper, level, TinkerWorld.spawnEnderGeode, "Ender geode biome modifier");
        assertRegistryEntry(helper, level, TinkerWorld.spawnCobaltOre, "Cobalt ore biome modifier");
        assertRegistryEntry(helper, level, TinkerWorld.spawnOverworldSlime, "Overworld slime biome modifier");
        assertRegistryEntry(helper, level, TinkerWorld.spawnTerracube, "Terracube biome modifier");
        assertRegistryEntry(helper, level, TinkerWorld.spawnEndSlime, "End slime biome modifier");

        helper.assertTrue(level.getServer().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("tconstruct", "structures/islands/sky/0x1x0.nbt")).isPresent(),
            "Sky slime island template NBT must be present in the loaded data pack resources");
        helper.assertTrue(level.getServer().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("tconstruct", "structures/islands/dirt/0x1x0.nbt")).isPresent(),
            "Dirt island template NBT must be present in the loaded data pack resources");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void worldgenConfiguredFeaturesPlaceExpectedBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var generator = level.getChunkSource().getGenerator();

        BlockPos oreOrigin = helper.absolutePos(new BlockPos(4, 16, 4));
        fillBox(level, oreOrigin.offset(-8, -8, -8), oreOrigin.offset(8, 8, 8), Blocks.NETHERRACK.defaultBlockState());
        primeWorldgenHeightmaps(level, oreOrigin.offset(-8, -8, -8), oreOrigin.offset(8, 8, 8));
        ConfiguredFeature<?, ?> smallCobalt = getConfiguredFeature(helper, level, TinkerWorld.configuredSmallCobaltOre, "Configured small cobalt ore");
        boolean orePlaced = smallCobalt.place(level, generator, RandomSource.create(0xC0BA17L), oreOrigin);
        int cobaltBlocks = countBlock(level, oreOrigin.offset(-8, -8, -8), oreOrigin.offset(8, 8, 8), TinkerWorld.cobaltOre.get());
        helper.assertTrue(orePlaced, "Configured small cobalt ore feature must report successful placement in a netherrack volume");
        helper.assertTrue(cobaltBlocks > 0, "Configured small cobalt ore feature must replace netherrack with cobalt ore; count=" + cobaltBlocks);

        BlockPos geodeOrigin = helper.absolutePos(new BlockPos(36, 24, 4));
        fillBox(level, geodeOrigin.offset(-20, -20, -20), geodeOrigin.offset(20, 20, 20), Blocks.STONE.defaultBlockState());
        primeWorldgenHeightmaps(level, geodeOrigin.offset(-20, -20, -20), geodeOrigin.offset(20, 20, 20));
        ConfiguredFeature<?, ?> earthGeode = getConfiguredFeature(helper, level, TinkerWorld.configuredEarthGeode, "Configured earth geode");
        boolean geodePlaced = earthGeode.place(level, generator, RandomSource.create(0xE417_6E0DEL), geodeOrigin);
        int crystalBlocks = countBlock(level, geodeOrigin.offset(-20, -20, -20), geodeOrigin.offset(20, 20, 20), TinkerWorld.earthGeode.getBlock());
        int buddingBlocks = countBlock(level, geodeOrigin.offset(-20, -20, -20), geodeOrigin.offset(20, 20, 20), TinkerWorld.earthGeode.getBudding());
        int calciteBlocks = countBlock(level, geodeOrigin.offset(-20, -20, -20), geodeOrigin.offset(20, 20, 20), Blocks.CALCITE);
        helper.assertTrue(geodePlaced, "Configured earth geode feature must report successful placement in a stone volume");
        helper.assertTrue(crystalBlocks > 0,
            "Configured earth geode must place earth slime crystal blocks; crystalBlocks=" + crystalBlocks
                + ", buddingBlocks=" + buddingBlocks + ", calciteBlocks=" + calciteBlocks);
        helper.assertTrue(buddingBlocks > 0,
            "Configured earth geode must place budding earth slime crystal blocks; crystalBlocks=" + crystalBlocks
                + ", buddingBlocks=" + buddingBlocks + ", calciteBlocks=" + calciteBlocks);
        helper.assertTrue(calciteBlocks > 0,
            "Configured earth geode must place its middle calcite layer; crystalBlocks=" + crystalBlocks
                + ", buddingBlocks=" + buddingBlocks + ", calciteBlocks=" + calciteBlocks);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void tableBlocksCreateMenusAndExposeItemHandlers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        assertTableMenuAndCapability(helper, level, new BlockPos(1, 1, 1), TinkerTables.craftingStation.get(), CraftingStationBlockEntity.class,
            CraftingStationContainerMenu.class, TinkerTables.craftingStationContainer.get(), 9, "Crafting station");
        assertTableMenuAndCapability(helper, level, new BlockPos(3, 1, 1), TinkerTables.partBuilder.get(), PartBuilderBlockEntity.class,
            PartBuilderContainerMenu.class, TinkerTables.partBuilderContainer.get(), 2, "Part builder");
        assertTableMenuAndCapability(helper, level, new BlockPos(1, 1, 3), TinkerTables.tinkerStation.get(), TinkerStationBlockEntity.class,
            TinkerStationContainerMenu.class, TinkerTables.tinkerStationContainer.get(), 4, "Tinker station");
        assertTableMenuAndCapability(helper, level, new BlockPos(3, 1, 3), TinkerTables.modifierWorktable.get(), ModifierWorktableBlockEntity.class,
            ModifierWorktableContainerMenu.class, TinkerTables.modifierWorktableContainer.get(), 3, "Modifier worktable");
        assertTableMenuAndCapability(helper, level, new BlockPos(5, 1, 3), TinkerTables.tinkersAnvil.get(), TinkerStationBlockEntity.class,
            TinkerStationContainerMenu.class, TinkerTables.tinkerStationContainer.get(), 6, "Tinkers' anvil");
        assertTableMenuAndCapability(helper, level, new BlockPos(1, 1, 5), TinkerTables.scorchedAnvil.get(), TinkerStationBlockEntity.class,
            TinkerStationContainerMenu.class, TinkerTables.tinkerStationContainer.get(), 6, "Scorched anvil");
        assertTableMenuAndCapability(helper, level, new BlockPos(3, 1, 5), TinkerTables.tinkersChest.get(), TinkersChestBlockEntity.class,
            TinkerChestContainerMenu.class, TinkerTables.tinkerChestContainer.get(), 1, "Tinkers' chest");
        assertTableMenuAndCapability(helper, level, new BlockPos(5, 1, 5), TinkerTables.partChest.get(), PartChestBlockEntity.class,
            TinkerChestContainerMenu.class, TinkerTables.tinkerChestContainer.get(), 1, "Part chest");
        assertTableMenuAndCapability(helper, level, new BlockPos(7, 1, 5), TinkerTables.castChest.get(), CastChestBlockEntity.class,
            TinkerChestContainerMenu.class, TinkerTables.tinkerChestContainer.get(), 1, "Cast chest");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 80)
    public static void networkPacketsRoundTripRepresentativePayloads(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 3));

        InventorySlotSyncPacket inventory = roundTrip(level, InventorySlotSyncPacket.STREAM_CODEC,
            new InventorySlotSyncPacket(new ItemStack(Items.IRON_INGOT, 3), 2, pos));
        helper.assertTrue(inventory.type() == InventorySlotSyncPacket.TYPE, "Inventory sync packet must keep its payload type");
        helper.assertTrue(inventory.pos().equals(pos), "Inventory sync packet must preserve block position; decoded=" + inventory.pos());
        helper.assertTrue(inventory.slot() == 2, "Inventory sync packet must preserve short-encoded slot; decoded=" + inventory.slot());
        helper.assertTrue(inventory.itemStack().is(Items.IRON_INGOT) && inventory.itemStack().getCount() == 3,
            "Inventory sync packet must preserve optional ItemStack payload; decoded=" + inventory.itemStack());

        SmelteryTankUpdatePacket tank = roundTrip(level, SmelteryTankUpdatePacket.STREAM_CODEC,
            new SmelteryTankUpdatePacket(pos, List.of(new FluidStack(Fluids.LAVA, 250), new FluidStack(TinkerFluids.moltenIron.get(), 90))));
        helper.assertTrue(tank.type() == SmelteryTankUpdatePacket.TYPE, "Smeltery tank packet must keep its payload type");
        helper.assertTrue(tank.fluids().size() == 2, "Smeltery tank packet must preserve fluid list size; decoded=" + tank.fluids());
        helper.assertTrue(tank.fluids().get(0).is(Fluids.LAVA) && tank.fluids().get(0).getAmount() == 250,
            "Smeltery tank packet must preserve first FluidStack; decoded=" + tank.fluids().get(0));
        helper.assertTrue(tank.fluids().get(1).is(TinkerFluids.moltenIron.get()) && tank.fluids().get(1).getAmount() == 90,
            "Smeltery tank packet must preserve molten iron FluidStack; decoded=" + tank.fluids().get(1));

        StructureErrorPositionPacket errorWithPos = roundTrip(level, StructureErrorPositionPacket.STREAM_CODEC,
            new StructureErrorPositionPacket(pos, pos.above()));
        StructureErrorPositionPacket errorWithoutPos = roundTrip(level, StructureErrorPositionPacket.STREAM_CODEC,
            new StructureErrorPositionPacket(pos, null));
        helper.assertTrue(errorWithPos.errorPos() != null && errorWithPos.errorPos().equals(pos.above()),
            "Structure error packet must preserve nullable error position when present; decoded=" + errorWithPos.errorPos());
        helper.assertTrue(errorWithoutPos.errorPos() == null,
            "Structure error packet must preserve nullable error position when absent; decoded=" + errorWithoutPos.errorPos());

        ChannelFlowPacket channel = roundTrip(level, ChannelFlowPacket.STREAM_CODEC, new ChannelFlowPacket(pos, Direction.EAST, true));
        helper.assertTrue(channel.side() == Direction.EAST && channel.flow(),
            "Channel flow packet must preserve direction and flow flag; decoded=" + channel);

        SmelteryFluidClickedPacket clicked = roundTrip(level, SmelteryFluidClickedPacket.STREAM_CODEC, new SmelteryFluidClickedPacket(4));
        helper.assertTrue(clicked.index() == 4, "Smeltery fluid click packet must preserve clicked tank index; decoded=" + clicked.index());

        TinkerControlPacket control = roundTrip(level, TinkerControlPacket.STREAM_CODEC, TinkerControlPacket.START_HELMET_INTERACT_SHIFT);
        helper.assertTrue(control == TinkerControlPacket.START_HELMET_INTERACT_SHIFT,
            "Tinker control packet must preserve enum command; decoded=" + control);

        PushBlockRowPacket push = roundTrip(level, PushBlockRowPacket.STREAM_CODEC, new PushBlockRowPacket(pos, Direction.SOUTH, false, 5));
        helper.assertTrue(push.pos().equals(pos) && push.direction() == Direction.SOUTH && !push.push() && push.moving() == 5,
            "Push block row packet must preserve position, direction, push flag, and moving count; decoded=" + push);

        ToolContainerFluidUpdatePacket toolFluid = roundTrip(level, ToolContainerFluidUpdatePacket.STREAM_CODEC,
            new ToolContainerFluidUpdatePacket(new FluidStack(TinkerFluids.venom.get(), 125)));
        helper.assertTrue(toolFluid.fluid().is(TinkerFluids.venom.get()) && toolFluid.fluid().getAmount() == 125,
            "Tool container fluid packet must preserve optional FluidStack payload; decoded=" + toolFluid.fluid());

        ResourceLocation layout = ResourceLocation.fromNamespaceAndPath("tconstruct", "pickaxe");
        TinkerStationSelectionPacket selection = roundTrip(level, TinkerStationSelectionPacket.STREAM_CODEC, new TinkerStationSelectionPacket(layout));
        helper.assertTrue(layout.equals(getPrivateField(selection, "layoutName", ResourceLocation.class)),
            "Tinker station selection packet must preserve layout id");

        TinkerStationRenamePacket rename = roundTrip(level, TinkerStationRenamePacket.STREAM_CODEC, new TinkerStationRenamePacket("Port Test Tool"));
        helper.assertTrue("Port Test Tool".equals(getPrivateField(rename, "name", String.class)),
            "Tinker station rename packet must preserve tool name");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void partBuilderCraftsFlintSmallBlade(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, TinkerTables.partBuilder.get().defaultBlockState(), Block.UPDATE_ALL);

        PartBuilderBlockEntity partBuilder = (PartBuilderBlockEntity) level.getBlockEntity(pos);
        helper.assertTrue(partBuilder != null, "Part builder must create a block entity for recipe crafting");
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.MATERIAL.get()).size() > 0,
            "Material recipes must be loaded for part builder material lookup");
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.PART_BUILDER.get()).size() > 0,
            "Part builder recipes must be loaded for pattern buttons");

        partBuilder.setItem(PartBuilderBlockEntity.PATTERN_SLOT, new ItemStack(TinkerTables.pattern.get()));
        partBuilder.setItem(PartBuilderBlockEntity.MATERIAL_SLOT, new ItemStack(Items.FLINT, 2));
        List<Pattern> buttons = partBuilder.getSortedButtons();
        ResourceLocation smallBladePattern = ResourceLocation.fromNamespaceAndPath("tconstruct", "small_blade");
        int smallBladeIndex = -1;
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).getLocation().equals(smallBladePattern)) {
                smallBladeIndex = i;
                break;
            }
        }
        helper.assertTrue(smallBladeIndex >= 0,
            "Flint plus blank pattern must expose the imported small blade recipe among all craftable full-import buttons; buttons=" + buttons);

        partBuilder.selectRecipe(smallBladeIndex);
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        ItemStack result = partBuilder.getCraftingResult().calcResult(player);
        helper.assertTrue(result.is(TinkerToolParts.smallBlade.get()), "Part builder must craft a small blade from flint; result=" + result);
        helper.assertTrue(MaterialIds.flint.matchesVariant(IMaterialItem.getMaterialFromStack(result)),
            "Small blade must keep flint material data; material=" + IMaterialItem.getMaterialFromStack(result));

        partBuilder.getCraftingResult().craftResult(player, result.copy(), 1);
        helper.assertTrue(partBuilder.getItem(PartBuilderBlockEntity.MATERIAL_SLOT).isEmpty(),
            "Part builder must consume two flint for a cost 2 small blade; remaining=" + partBuilder.getItem(PartBuilderBlockEntity.MATERIAL_SLOT));
        helper.assertTrue(partBuilder.getItem(PartBuilderBlockEntity.PATTERN_SLOT).isEmpty(),
            "Part builder must consume the non-reusable blank pattern after crafting");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void tinkerStationBuildsDaggerFromToolParts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, TinkerTables.tinkerStation.get().defaultBlockState(), Block.UPDATE_ALL);

        TinkerStationBlockEntity station = (TinkerStationBlockEntity) level.getBlockEntity(pos);
        helper.assertTrue(station != null, "Tinker station must create a block entity for tool building");
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.TINKER_STATION.get()).stream()
                .anyMatch(holder -> holder.id().equals(ResourceLocation.fromNamespaceAndPath("tconstruct", "tools/building/dagger"))),
            "Upstream dagger tool building recipe must be loaded");

        ItemStack blade = TinkerToolParts.smallBlade.get().withMaterial(MaterialIds.flint);
        ItemStack handle = TinkerToolParts.toolHandle.get().withMaterial(MaterialIds.wood);
        station.setItem(TinkerStationBlockEntity.INPUT_SLOT, blade);
        station.setItem(TinkerStationBlockEntity.INPUT_SLOT + 1, handle);

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        ItemStack result = station.getCraftingResult().calcResult(player);
        helper.assertTrue(result.is(TinkerTools.dagger.get()), "Tinker station must craft a dagger from blade and handle; result=" + result);
        helper.assertTrue(result.getCount() == 2, "Dagger recipe must preserve upstream result_count=2; count=" + result.getCount());

        ToolStack tool = ToolStack.from(result);
        helper.assertTrue(tool.getMaterials().size() == 2, "Dagger must have two material entries; materials=" + tool.getMaterials());
        helper.assertTrue(MaterialIds.flint.matchesVariant(tool.getMaterials().get(0).getVariant()),
            "Dagger blade material must come from the small blade input; materials=" + tool.getMaterials());
        helper.assertTrue(MaterialIds.wood.matchesVariant(tool.getMaterials().get(1).getVariant()),
            "Dagger handle material must come from the tool handle input; materials=" + tool.getMaterials());
        helper.assertTrue(tool.tryValidate() == null, "Dagger built by Tinker Station must validate; error=" + tool.tryValidate());

        station.getCraftingResult().craftResult(player, result.copy(), result.getCount());
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.INPUT_SLOT).isEmpty(),
            "Tinker station must consume the blade input after building a dagger");
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.INPUT_SLOT + 1).isEmpty(),
            "Tinker station must consume the handle input after building a dagger");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void tinkersAnvilBuildsSledgeHammerFromToolParts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, TinkerTables.tinkersAnvil.get().defaultBlockState(), Block.UPDATE_ALL);

        TinkerStationBlockEntity anvil = (TinkerStationBlockEntity) level.getBlockEntity(pos);
        helper.assertTrue(anvil != null, "Tinkers' anvil must create a Tinker Station block entity");
        helper.assertTrue(anvil.getContainerSize() == 6,
            "Tinkers' anvil must expose the tool slot plus five input slots; size=" + anvil.getContainerSize());
        assertTinkerStationRecipeLoaded(helper, level, "tools/building/sledge_hammer", "Upstream sledge hammer tool building recipe");

        anvil.setItem(TinkerStationBlockEntity.INPUT_SLOT, TinkerToolParts.hammerHead.get().withMaterial(MaterialIds.iron));
        anvil.setItem(TinkerStationBlockEntity.INPUT_SLOT + 1, TinkerToolParts.toughHandle.get().withMaterial(MaterialIds.wood));
        anvil.setItem(TinkerStationBlockEntity.INPUT_SLOT + 2, TinkerToolParts.largePlate.get().withMaterial(MaterialIds.iron));
        anvil.setItem(TinkerStationBlockEntity.INPUT_SLOT + 3, TinkerToolParts.largePlate.get().withMaterial(MaterialIds.iron));

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        ItemStack result = anvil.getCraftingResult().calcResult(player);
        helper.assertTrue(result.is(TinkerTools.sledgeHammer.get()), "Tinkers' anvil must craft a sledge hammer from four tool parts; result=" + result);
        helper.assertTrue(result.getCount() == 1, "Sledge hammer recipe must preserve upstream result_count=1; count=" + result.getCount());
        helper.assertTrue(anvil.getLastRecipe() != null && anvil.getLastRecipe().getSerializer() == TinkerTables.toolBuildingRecipeSerializer.get(),
            "Tinkers' anvil must select the imported tool building recipe; recipe=" + anvil.getLastRecipe());

        ToolStack tool = ToolStack.from(result);
        helper.assertTrue(tool.getMaterials().size() == 4, "Sledge hammer must have four material entries; materials=" + tool.getMaterials());
        helper.assertTrue(MaterialIds.iron.matchesVariant(tool.getMaterials().get(0).getVariant()),
            "Sledge hammer head material must come from the hammer head input; materials=" + tool.getMaterials());
        helper.assertTrue(MaterialIds.wood.matchesVariant(tool.getMaterials().get(1).getVariant()),
            "Sledge hammer handle material must come from the tough handle input; materials=" + tool.getMaterials());
        helper.assertTrue(MaterialIds.iron.matchesVariant(tool.getMaterials().get(2).getVariant())
                && MaterialIds.iron.matchesVariant(tool.getMaterials().get(3).getVariant()),
            "Sledge hammer plate materials must come from the large plate inputs; materials=" + tool.getMaterials());
        helper.assertTrue(tool.tryValidate() == null, "Sledge hammer built by Tinkers' Anvil must validate; error=" + tool.tryValidate());

        anvil.getCraftingResult().craftResult(player, result.copy(), result.getCount());
        helper.assertTrue(anvil.getItem(TinkerStationBlockEntity.INPUT_SLOT).isEmpty(),
            "Tinkers' anvil must consume the hammer head input after building a sledge hammer");
        helper.assertTrue(anvil.getItem(TinkerStationBlockEntity.INPUT_SLOT + 1).isEmpty(),
            "Tinkers' anvil must consume the tough handle input after building a sledge hammer");
        helper.assertTrue(anvil.getItem(TinkerStationBlockEntity.INPUT_SLOT + 2).isEmpty(),
            "Tinkers' anvil must consume the first large plate input after building a sledge hammer");
        helper.assertTrue(anvil.getItem(TinkerStationBlockEntity.INPUT_SLOT + 3).isEmpty(),
            "Tinkers' anvil must consume the second large plate input after building a sledge hammer");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void tinkerStationSwapsToolPartMaterials(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertTinkerStationRecipeLoaded(helper, level, "tables/tinker_station_part_swapping", "Upstream Tinker Station part swapping recipe");
        assertTinkerStationRecipeLoaded(helper, level, "tables/tool_material_swapping", "Upstream tool material swapping recipe");
        assertTinkerStationRecipeLoaded(helper, level, "tables/ammo_part_swapping", "Upstream ammo part swapping recipe");
        assertTinkerStationRecipeLoaded(helper, level, "tables/throwing_axe_part_swapping", "Upstream throwing axe part swapping recipe");
        assertTinkerStationRecipeLoaded(helper, level, "tables/tinker_station_repair", "Upstream Tinker Station repair recipe");
        assertCraftingRecipeLoaded(helper, level, "tables/crafting_table_repair", "Upstream crafting table repair recipe");

        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, TinkerTables.tinkerStation.get().defaultBlockState(), Block.UPDATE_ALL);

        TinkerStationBlockEntity station = (TinkerStationBlockEntity) level.getBlockEntity(pos);
        helper.assertTrue(station != null, "Tinker station must create a block entity for part swapping");

        ItemStack daggerStack = ToolBuildHandler.buildItemFromMaterials(TinkerTools.dagger.get(), MaterialNBT.builder()
            .add(MaterialIds.flint)
            .add(MaterialIds.wood)
            .build());
        ToolStack dagger = ToolStack.from(daggerStack);
        int originalDamage = Math.min(10, Math.max(1, dagger.getStats().getInt(ToolStats.DURABILITY) / 4));
        dagger.setDamage(originalDamage);
        station.setItem(TinkerStationBlockEntity.TINKER_SLOT, dagger.createStack());
        station.setItem(TinkerStationBlockEntity.INPUT_SLOT, TinkerToolParts.smallBlade.get().withMaterial(MaterialIds.iron));

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        ItemStack result = station.getCraftingResult().calcResult(player);
        helper.assertTrue(result.is(TinkerTools.dagger.get()), "Part swapping must return a dagger result; result=" + result);
        helper.assertTrue(station.getLastRecipe() != null && station.getLastRecipe().getSerializer() == TinkerTables.tinkerStationPartSwappingSerializer.get(),
            "Tinker Station must select the imported part swapping recipe; recipe=" + station.getLastRecipe());

        ToolStack swapped = ToolStack.from(result);
        helper.assertTrue(swapped.getMaterials().size() == 2, "Swapped dagger must keep two material slots; materials=" + swapped.getMaterials());
        helper.assertTrue(MaterialIds.iron.matchesVariant(swapped.getMaterials().get(0).getVariant()),
            "Part swapping must replace the dagger blade material with iron; materials=" + swapped.getMaterials());
        helper.assertTrue(MaterialIds.wood.matchesVariant(swapped.getMaterials().get(1).getVariant()),
            "Part swapping must preserve the dagger handle material; materials=" + swapped.getMaterials());
        helper.assertTrue(swapped.getDamage() <= originalDamage,
            "Part swapping must preserve or repair existing tool damage based on the imported material repair value; before=" + originalDamage + ", after=" + swapped.getDamage());
        helper.assertTrue(swapped.tryValidate() == null, "Part-swapped dagger must validate; error=" + swapped.tryValidate());

        station.getCraftingResult().craftResult(player, result.copy(), result.getCount());
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.TINKER_SLOT).isEmpty(),
            "Part swapping must consume the original tool stack when the result is taken");
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.INPUT_SLOT).isEmpty(),
            "Part swapping must consume the replacement tool part");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void tinkerStationRepairsToolWithRepairKit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertTinkerStationRecipeLoaded(helper, level, "tables/tinker_station_repair", "Upstream Tinker Station repair recipe");
        assertCraftingRecipeLoaded(helper, level, "tables/crafting_table_repair", "Upstream crafting table repair recipe");

        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, TinkerTables.tinkerStation.get().defaultBlockState(), Block.UPDATE_ALL);

        TinkerStationBlockEntity station = (TinkerStationBlockEntity) level.getBlockEntity(pos);
        helper.assertTrue(station != null, "Tinker station must create a block entity for tool repair");

        ItemStack daggerStack = ToolBuildHandler.buildItemFromMaterials(TinkerTools.dagger.get(), MaterialNBT.builder()
            .add(MaterialIds.iron)
            .add(MaterialIds.wood)
            .build());
        ToolStack dagger = ToolStack.from(daggerStack);
        helper.assertTrue(MaterialRepairToolHook.canRepairWith(dagger, MaterialIds.iron),
            "Iron-headed dagger must accept iron as a repair material; materials=" + dagger.getMaterials());
        helper.assertTrue(MaterialRepairToolHook.repairAmount(dagger, MaterialIds.iron) > 0,
            "Iron-headed dagger must have a positive iron repair amount; amount=" + MaterialRepairToolHook.repairAmount(dagger, MaterialIds.iron));

        int originalDamage = Math.min(40, Math.max(1, dagger.getStats().getInt(ToolStats.DURABILITY) / 3));
        dagger.setDamage(originalDamage);
        station.setItem(TinkerStationBlockEntity.TINKER_SLOT, dagger.createStack());
        station.setItem(TinkerStationBlockEntity.INPUT_SLOT, TinkerToolParts.repairKit.get().withMaterial(MaterialIds.iron));

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        ItemStack result = station.getCraftingResult().calcResult(player);
        helper.assertTrue(result.is(TinkerTools.dagger.get()), "Tinker Station repair must return a dagger result; result=" + result);
        helper.assertTrue(station.getLastRecipe() != null && station.getLastRecipe().getSerializer() == TinkerTables.tinkerStationRepairSerializer.get(),
            "Tinker Station must select the imported repair recipe; recipe=" + station.getLastRecipe());

        ToolStack repaired = ToolStack.from(result);
        helper.assertTrue(repaired.getDamage() < originalDamage,
            "Repair kit must reduce tool damage; before=" + originalDamage + ", after=" + repaired.getDamage());
        helper.assertTrue(MaterialIds.iron.matchesVariant(repaired.getMaterials().get(0).getVariant()),
            "Repair must preserve the dagger blade material; materials=" + repaired.getMaterials());
        helper.assertTrue(MaterialIds.wood.matchesVariant(repaired.getMaterials().get(1).getVariant()),
            "Repair must preserve the dagger handle material; materials=" + repaired.getMaterials());
        helper.assertTrue(repaired.tryValidate() == null, "Repaired dagger must validate; error=" + repaired.tryValidate());

        station.getCraftingResult().craftResult(player, result.copy(), result.getCount());
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.TINKER_SLOT).isEmpty(),
            "Tinker Station repair must consume the damaged tool stack when the result is taken");
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.INPUT_SLOT).isEmpty(),
            "Tinker Station repair must consume the repair kit");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void craftingTableRepairsToolWithRepairKit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertCraftingRecipeLoaded(helper, level, "tables/crafting_table_repair", "Upstream crafting table repair recipe");

        ItemStack daggerStack = ToolBuildHandler.buildItemFromMaterials(TinkerTools.dagger.get(), MaterialNBT.builder()
            .add(MaterialIds.iron)
            .add(MaterialIds.wood)
            .build());
        ToolStack dagger = ToolStack.from(daggerStack);
        int originalDamage = Math.min(40, Math.max(1, dagger.getStats().getInt(ToolStats.DURABILITY) / 3));
        dagger.setDamage(originalDamage);

        AbstractContainerMenu menu = new AbstractContainerMenu(null, 0) {
            @Override
            public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(net.minecraft.world.entity.player.Player player) {
                return true;
            }
        };
        TransientCraftingContainer crafting = new TransientCraftingContainer(menu, 3, 3);
        crafting.setItem(0, dagger.createStack());
        crafting.setItem(1, TinkerToolParts.repairKit.get().withMaterial(MaterialIds.iron));
        var input = crafting.asCraftInput();

        RecipeHolder<CraftingRecipe> recipe = level.getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, input, level, (RecipeHolder<CraftingRecipe>) null)
            .orElse(null);
        helper.assertTrue(recipe != null, "Crafting table must find a recipe for a damaged dagger plus matching repair kit");
        helper.assertTrue(recipe.id().equals(ResourceLocation.fromNamespaceAndPath("tconstruct", "tables/crafting_table_repair")),
            "Crafting table repair must select the imported TConstruct recipe; recipe=" + (recipe == null ? null : recipe.id()));

        ItemStack result = recipe.value().assemble(input, level.registryAccess());
        helper.assertTrue(result.is(TinkerTools.dagger.get()), "Crafting table repair must return a dagger result; result=" + result);
        ToolStack repaired = ToolStack.from(result);
        helper.assertTrue(repaired.getDamage() < originalDamage,
            "Crafting table repair kit must reduce tool damage; before=" + originalDamage + ", after=" + repaired.getDamage());
        helper.assertTrue(MaterialIds.iron.matchesVariant(repaired.getMaterials().get(0).getVariant()),
            "Crafting table repair must preserve the dagger blade material; materials=" + repaired.getMaterials());
        helper.assertTrue(MaterialIds.wood.matchesVariant(repaired.getMaterials().get(1).getVariant()),
            "Crafting table repair must preserve the dagger handle material; materials=" + repaired.getMaterials());
        helper.assertTrue(repaired.tryValidate() == null, "Crafting-table repaired dagger must validate; error=" + repaired.tryValidate());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void tinkerStationAppliesDiamondModifier(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, TinkerTables.tinkerStation.get().defaultBlockState(), Block.UPDATE_ALL);

        TinkerStationBlockEntity station = (TinkerStationBlockEntity) level.getBlockEntity(pos);
        helper.assertTrue(station != null, "Tinker station must create a block entity for modifier application");
        helper.assertTrue(new ItemStack(Items.DIAMOND).is(Tags.Items.GEMS_DIAMOND),
            "minecraft:diamond must be in " + Tags.Items.GEMS_DIAMOND.location() + " for the diamond modifier recipe");
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.TINKER_STATION.get()).stream()
                .anyMatch(holder -> holder.id().equals(ResourceLocation.fromNamespaceAndPath("tconstruct", "tools/modifiers/upgrade/diamond"))),
            "Upstream diamond modifier recipe must be loaded");

        ItemStack dagger = ToolBuildHandler.buildItemFromMaterials(TinkerTools.dagger.get(), MaterialNBT.builder()
            .add(MaterialIds.flint)
            .add(MaterialIds.wood)
            .build());
        ToolStack before = ToolStack.from(dagger);
        helper.assertTrue(before.getUpgrades().getLevel(ModifierIds.diamond) == 0,
            "Fresh dagger must not already have diamond; upgrades=" + before.getUpgrades());
        helper.assertTrue(before.getFreeSlots(SlotType.UPGRADE) >= 1,
            "Fresh dagger must have an upgrade slot for diamond; freeUpgrades=" + before.getFreeSlots(SlotType.UPGRADE));

        station.setItem(TinkerStationBlockEntity.TINKER_SLOT, dagger);
        station.setItem(TinkerStationBlockEntity.INPUT_SLOT, new ItemStack(Items.DIAMOND));

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        ItemStack result = station.getCraftingResult().calcResult(player);
        helper.assertTrue(result.is(TinkerTools.dagger.get()), "Diamond modifier recipe must return the modified dagger; result=" + result);
        ToolStack after = ToolStack.from(result);
        helper.assertTrue(after.getUpgrades().getLevel(ModifierIds.diamond) == 1,
            "Diamond modifier must be stored as a level 1 upgrade; upgrades=" + after.getUpgrades());
        helper.assertTrue(after.getFreeSlots(SlotType.UPGRADE) == before.getFreeSlots(SlotType.UPGRADE) - 1,
            "Diamond modifier must consume one upgrade slot; before=" + before.getFreeSlots(SlotType.UPGRADE)
                + ", after=" + after.getFreeSlots(SlotType.UPGRADE));
        helper.assertTrue(after.tryValidate() == null, "Diamond-modified dagger must validate; error=" + after.tryValidate());

        station.getCraftingResult().craftResult(player, result.copy(), result.getCount());
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.INPUT_SLOT).isEmpty(),
            "Tinker station must consume the diamond input after applying the modifier");
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.TINKER_SLOT).isEmpty(),
            "Tinker station must consume the original tool stack when the modified result is taken");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void modifierWorktableSortsToolModifiers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/modifier_sorting", "Upstream modifier sorting worktable recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/toggle_interaction_modifier", "Upstream toggle interaction worktable recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/remove_modifier_sponge", "Upstream sponge modifier removal recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/remove_modifier_venom", "Upstream venom modifier removal recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/invisible_ink_adding", "Upstream invisible ink adding recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/invisible_ink_removing", "Upstream invisible ink removing recipe");

        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, TinkerTables.modifierWorktable.get().defaultBlockState(), Block.UPDATE_ALL);

        ModifierWorktableBlockEntity worktable = (ModifierWorktableBlockEntity) level.getBlockEntity(pos);
        helper.assertTrue(worktable != null, "Modifier worktable must create a block entity for worktable recipes");

        ItemStack daggerStack = ToolBuildHandler.buildItemFromMaterials(TinkerTools.dagger.get(), MaterialNBT.builder()
            .add(MaterialIds.flint)
            .add(MaterialIds.wood)
            .build());
        ToolStack dagger = ToolStack.from(daggerStack);
        dagger.addModifier(ModifierIds.diamond, 1);
        dagger.addModifier(ModifierIds.sharpness, 1);
        ItemStack sortableDagger = dagger.createStack();

        worktable.setItem(ModifierWorktableBlockEntity.TINKER_SLOT, sortableDagger);
        worktable.setItem(ModifierWorktableBlockEntity.INPUT_START, new ItemStack(Items.COMPASS));
        var recipe = worktable.getCurrentRecipe();
        helper.assertTrue(recipe != null, "Modifier worktable must find the sorting recipe for compass plus modifiable tool");
        helper.assertTrue(recipe instanceof ModifierSortingRecipe,
            "Modifier worktable must select the imported sorting recipe; recipe=" + recipe.getClass().getName());
        helper.assertTrue(worktable.getCurrentButtons().size() == 2,
            "Modifier worktable sorting must expose both upgrade modifiers as buttons; buttons=" + worktable.getCurrentButtons());
        helper.assertTrue(worktable.getCurrentButtons().get(0).getId().equals(ModifierIds.diamond),
            "First sortable modifier should initially be diamond; buttons=" + worktable.getCurrentButtons());
        helper.assertTrue(worktable.getCurrentButtons().get(1).getId().equals(ModifierIds.sharpness),
            "Second sortable modifier should initially be sharpness; buttons=" + worktable.getCurrentButtons());

        worktable.selectModifier(0);
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        ItemStack result = worktable.getCraftingResult().calcResult(player);
        helper.assertTrue(result.is(TinkerTools.dagger.get()), "Modifier sorting must return the same dagger item; result=" + result);
        ToolStack sorted = ToolStack.from(result);
        List<ModifierEntry> sortedUpgrades = sorted.getUpgrades().getModifiers();
        helper.assertTrue(sortedUpgrades.size() == 2, "Sorted dagger must keep both upgrade modifiers; upgrades=" + sortedUpgrades);
        helper.assertTrue(sortedUpgrades.get(0).getId().equals(ModifierIds.sharpness),
            "Selecting diamond with a compass must move it forward after sharpness; upgrades=" + sortedUpgrades);
        helper.assertTrue(sortedUpgrades.get(1).getId().equals(ModifierIds.diamond),
            "Diamond modifier must move to the second upgrade position; upgrades=" + sortedUpgrades);
        helper.assertTrue(sorted.tryValidate() == null, "Sorted dagger must still validate; error=" + sorted.tryValidate());

        worktable.getCraftingResult().craftResult(player, result.copy(), result.getCount());
        helper.assertTrue(worktable.getItem(ModifierWorktableBlockEntity.INPUT_START).is(Items.COMPASS),
            "Modifier sorting must keep the compass catalyst after crafting");
        helper.assertTrue(worktable.getItem(ModifierWorktableBlockEntity.TINKER_SLOT).isEmpty(),
            "Modifier worktable must consume the original tool stack when the sorted result is taken");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void modifierWorktableExtractsUpgradeIntoCrystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/extract/upgrade", "Upstream generic upgrade extraction recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/extract/upgrade_dagger", "Upstream dagger upgrade extraction recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/extract/ability", "Upstream ability extraction recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/extract/ability_dagger", "Upstream dagger ability extraction recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/extract/defense", "Upstream defense extraction recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/extract/defense_dagger", "Upstream dagger defense extraction recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/extract/modifier", "Upstream general modifier extraction recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/extract/modifier_dagger", "Upstream dagger general modifier extraction recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/extract/slotless", "Upstream slotless extraction recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/extract/slotless_dagger", "Upstream dagger slotless extraction recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/enchantment_converting/ability_book", "Upstream ability enchanted-book conversion recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/enchantment_converting/ability_tool", "Upstream ability enchanted-tool conversion recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/enchantment_converting/defense_book", "Upstream defense enchanted-book conversion recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/enchantment_converting/defense_tool", "Upstream defense enchanted-tool conversion recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/enchantment_converting/slotless_book", "Upstream slotless enchanted-book conversion recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/enchantment_converting/slotless_tool", "Upstream slotless enchanted-tool conversion recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/enchantment_converting/upgrade_book", "Upstream upgrade enchanted-book conversion recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/enchantment_converting/upgrade_tool", "Upstream upgrade enchanted-tool conversion recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/enchantment_converting/unenchant_book", "Upstream enchanted-book disenchanting recipe");
        assertModifierWorktableRecipeLoaded(helper, level, "tools/modifiers/worktable/enchantment_converting/unenchant_tool", "Upstream enchanted-tool disenchanting recipe");

        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, TinkerTables.modifierWorktable.get().defaultBlockState(), Block.UPDATE_ALL);

        ModifierWorktableBlockEntity worktable = (ModifierWorktableBlockEntity) level.getBlockEntity(pos);
        helper.assertTrue(worktable != null, "Modifier worktable must create a block entity for extraction recipes");

        ItemStack warPickStack = ToolBuildHandler.buildItemFromMaterials(TinkerTools.warPick.get(), MaterialNBT.builder()
            .add(MaterialIds.iron)
            .add(MaterialIds.wood)
            .add(MaterialIds.string)
            .build());
        ToolStack warPick = ToolStack.from(warPickStack);
        warPick.addModifier(ModifierIds.diamond, 1);
        helper.assertTrue(warPick.getUpgrades().getLevel(ModifierIds.diamond) == 1,
            "Prepared war pick must contain the diamond upgrade before extraction; upgrades=" + warPick.getUpgrades());

        worktable.setItem(ModifierWorktableBlockEntity.TINKER_SLOT, warPick.createStack());
        worktable.setItem(ModifierWorktableBlockEntity.INPUT_START, new ItemStack(TinkerWorld.skyGeode.get()));
        worktable.setItem(ModifierWorktableBlockEntity.INPUT_START + 1, new ItemStack(Items.WET_SPONGE));
        var recipe = worktable.getCurrentRecipe();
        helper.assertTrue(recipe != null, "Modifier worktable must find an extraction recipe for sky crystal plus wet sponge");
        List<ModifierEntry> buttons = worktable.getCurrentButtons();
        int diamondIndex = -1;
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).getId().equals(ModifierIds.diamond)) {
                diamondIndex = i;
                break;
            }
        }
        helper.assertTrue(diamondIndex >= 0, "Extraction recipe must expose diamond as a removable upgrade; buttons=" + buttons);

        worktable.selectModifier(diamondIndex);
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        ItemStack result = worktable.getCraftingResult().calcResult(player);
        helper.assertTrue(result.is(TinkerTools.warPick.get()), "Modifier extraction must return the same tool item; result=" + result);
        ToolStack extracted = ToolStack.from(result);
        helper.assertTrue(extracted.getUpgrades().getLevel(ModifierIds.diamond) == 0,
            "Modifier extraction must remove one diamond upgrade from the result tool; upgrades=" + extracted.getUpgrades());
        helper.assertTrue(extracted.tryValidate() == null, "Extracted war pick must still validate; error=" + extracted.tryValidate());

        worktable.getCraftingResult().craftResult(player, result.copy(), result.getCount());
        helper.assertTrue(worktable.getItem(ModifierWorktableBlockEntity.TINKER_SLOT).isEmpty(),
            "Modifier extraction must consume the original tool stack when the result is taken");
        helper.assertTrue(worktable.getItem(ModifierWorktableBlockEntity.INPUT_START).isEmpty(),
            "Modifier extraction must consume the sky slime crystal input");
        helper.assertTrue(worktable.getItem(ModifierWorktableBlockEntity.INPUT_START + 1).isEmpty(),
            "Modifier extraction must consume the wet sponge input");
        helper.assertTrue(playerHasModifierCrystal(player.getInventory(), ModifierIds.diamond),
            "Modifier extraction must give the player a diamond modifier crystal");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.SPONGE)),
            "Modifier extraction must return the wet sponge leftover as a regular sponge");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void dynamicStatBoostModifiersApplyFloatAndToolConditions(GameTestHelper helper) {
        ItemStack daggerStack = ToolBuildHandler.buildItemFromMaterials(TinkerTools.dagger.get(), MaterialNBT.builder()
            .add(MaterialIds.flint)
            .add(MaterialIds.wood)
            .build());
        ToolStack dagger = ToolStack.from(daggerStack);
        float baseAttack = dagger.getStats().get(ToolStats.ATTACK_DAMAGE);
        dagger.addModifier(ModifierIds.sharpness, 2);
        assertFloatEquals(helper, baseAttack + 1.5F, dagger.getStats().get(ToolStats.ATTACK_DAMAGE), 0.0001F,
            "Sharpness II must apply upstream stat_boost each_level=0.75 to attack damage");

        ItemStack longbowStack = ToolBuildHandler.buildItemFromMaterials(TinkerTools.longbow.get(), MaterialNBT.builder()
            .add(MaterialIds.wood)
            .add(MaterialIds.wood)
            .add(MaterialIds.wood)
            .add(MaterialIds.string)
            .build());
        helper.assertTrue(longbowStack.is(TinkerTags.Items.RANGED) && !longbowStack.is(TinkerTags.Items.AMMO),
            "Longbow must match the ranged-only side of crystalbound; stack=" + longbowStack);
        ToolStack longbow = ToolStack.from(longbowStack);
        float baseBowVelocity = longbow.getStats().get(ToolStats.VELOCITY);
        float baseBowProjectileDamage = longbow.getStats().get(ToolStats.PROJECTILE_DAMAGE);
        longbow.addModifier(ModifierIds.crystalbound, 1);
        assertFloatEquals(helper, baseBowVelocity + 0.1F, longbow.getStats().get(ToolStats.VELOCITY), 0.0001F,
            "Crystalbound on ranged tools must apply only the velocity stat_boost");
        assertFloatEquals(helper, baseBowProjectileDamage, longbow.getStats().get(ToolStats.PROJECTILE_DAMAGE), 0.0001F,
            "Crystalbound on ranged tools must not apply the ammo-only projectile damage stat_boost");

        ItemStack arrowStack = ToolBuildHandler.buildItemFromMaterials(TinkerTools.arrow.get(), MaterialNBT.builder()
            .add(MaterialIds.flint)
            .add(MaterialIds.wood)
            .add(MaterialIds.feather)
            .build());
        helper.assertTrue(arrowStack.is(TinkerTags.Items.AMMO) && !arrowStack.is(TinkerTags.Items.RANGED),
            "Material arrow must match the ammo-only side of crystalbound; stack=" + arrowStack);
        ToolStack arrow = ToolStack.from(arrowStack);
        float baseArrowVelocity = arrow.getStats().get(ToolStats.VELOCITY);
        float baseArrowProjectileDamage = arrow.getStats().get(ToolStats.PROJECTILE_DAMAGE);
        arrow.addModifier(ModifierIds.crystalbound, 1);
        assertFloatEquals(helper, baseArrowVelocity, arrow.getStats().get(ToolStats.VELOCITY), 0.0001F,
            "Crystalbound on ammo must not apply the ranged-only velocity stat_boost");
        assertFloatEquals(helper, baseArrowProjectileDamage + 0.75F, arrow.getStats().get(ToolStats.PROJECTILE_DAMAGE), 0.0001F,
            "Crystalbound on ammo must apply only the projectile damage stat_boost");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void plateChestplateBuildsWithArmorStatsAndAttributes(GameTestHelper helper) {
        ItemStack chestplate = ToolBuildHandler.buildItemFromMaterials(TinkerTools.plateArmor.get(ArmorItem.Type.CHESTPLATE), MaterialNBT.builder()
            .add(MaterialIds.iron)
            .add(MaterialIds.iron)
            .build());
        helper.assertTrue(chestplate.is(TinkerTools.plateArmor.get(ArmorItem.Type.CHESTPLATE)),
            "Plate chestplate build must return the registered chestplate item; result=" + chestplate);
        helper.assertTrue(chestplate.is(TinkerTags.Items.ARMOR),
            "Plate chestplate stack must be in the armor item tag; result=" + chestplate);

        ToolStack tool = ToolStack.from(chestplate);
        helper.assertTrue(tool.getMaterials().size() == 2, "Plate chestplate must have plating and maille material entries; materials=" + tool.getMaterials());
        helper.assertTrue(MaterialIds.iron.matchesVariant(tool.getMaterials().get(0).getVariant()),
            "Plate chestplate plating material must be iron; materials=" + tool.getMaterials());
        helper.assertTrue(MaterialIds.iron.matchesVariant(tool.getMaterials().get(1).getVariant()),
            "Plate chestplate maille material must be iron; materials=" + tool.getMaterials());
        helper.assertTrue(tool.getStats().get(ToolStats.ARMOR) > 0,
            "Plate chestplate must expose positive armor stats; stats=" + tool.getStats());
        helper.assertTrue(tool.getStats().get(ToolStats.DURABILITY) > 0,
            "Plate chestplate must expose positive durability stats; stats=" + tool.getStats());
        helper.assertTrue(tool.getFreeSlots(SlotType.UPGRADE) == 2,
            "Plate chestplate must keep its two upstream upgrade slots; freeUpgrades=" + tool.getFreeSlots(SlotType.UPGRADE));
        helper.assertTrue(tool.getFreeSlots(SlotType.DEFENSE) == 3,
            "Plate chestplate must keep its three upstream defense slots; freeDefense=" + tool.getFreeSlots(SlotType.DEFENSE));
        helper.assertTrue(tool.tryValidate() == null, "Plate chestplate built from armor materials must validate; error=" + tool.tryValidate());

        ModifiableArmorItem armorItem = (ModifiableArmorItem) chestplate.getItem();
        helper.assertTrue(armorItem.getEquipmentSlot() == EquipmentSlot.CHEST,
            "Plate chestplate must equip in the chest slot; slot=" + armorItem.getEquipmentSlot());
        helper.assertTrue(!armorItem.getAttributeModifiers(tool, EquipmentSlot.CHEST).get(Attributes.ARMOR.value()).isEmpty(),
            "Plate chestplate must provide armor attributes in the chest slot");
        helper.assertTrue(armorItem.getAttributeModifiers(tool, EquipmentSlot.HEAD).isEmpty(),
            "Plate chestplate must not provide armor attributes in the wrong slot");

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);
        helper.assertTrue(player.getItemBySlot(EquipmentSlot.CHEST).is(TinkerTools.plateArmor.get(ArmorItem.Type.CHESTPLATE)),
            "Mock player must keep the plate chestplate in the chest equipment slot");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void tconstructShieldsKeepBlockingStatsAndUseBehavior(GameTestHelper helper) {
        ItemStack travelersShield = ToolBuildHandler.buildItemFromMaterials(TinkerTools.travelersShield.get(), MaterialNBT.builder()
            .add(MaterialIds.wood)
            .build());
        assertShieldBasics(helper, travelersShield, TinkerTools.travelersShield.get(), 10, 90, 0.8F, 2, 2, 1);

        ItemStack plateShield = ToolBuildHandler.buildItemFromMaterials(TinkerTools.plateShield.get(), MaterialNBT.builder()
            .add(MaterialIds.wood)
            .add(MaterialIds.iron)
            .build());
        assertShieldBasics(helper, plateShield, TinkerTools.plateShield.get(), 100, 180, 0.2F, 2, 3, 0);

        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.OFF_HAND, plateShield);
        InteractionResult result = plateShield.getItem().use(helper.getLevel(), player, InteractionHand.OFF_HAND).getResult();
        helper.assertTrue(result.consumesAction(),
            "TConstruct shield right-click must start blocking use from the offhand; result=" + result);
        helper.assertTrue(player.isUsingItem(), "TConstruct shield use must put the player into active item use");
        helper.assertTrue(player.getUseItem().is(TinkerTools.plateShield.get()),
            "Active use item must be the plate shield; useItem=" + player.getUseItem());
        helper.assertTrue(plateShield.getItem().getUseAnimation(plateShield) == UseAnim.BLOCK,
            "TConstruct shield must render with vanilla block use animation");
        helper.assertTrue(plateShield.getItem().getUseDuration(plateShield, player) == 72000,
            "TConstruct shield blocking duration must match upstream blocking module");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void slimeBootsStaffsAndSpringingKeepGadgetBehavior(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        ItemStack slimeBoots = ToolBuildHandler.buildItemFromMaterials(TinkerTools.slimesuit.get(ArmorItem.Type.BOOTS), MaterialNBT.builder()
            .add(MaterialIds.skySlimeskin)
            .add(MaterialIds.blood)
            .build());
        helper.assertTrue(slimeBoots.is(TinkerTools.slimesuit.get(ArmorItem.Type.BOOTS)),
            "Slime boots build must return the registered slime boots item; result=" + slimeBoots);
        helper.assertTrue(slimeBoots.is(TinkerTags.Items.ARMOR), "Slime boots must be tagged as modifiable armor");
        helper.assertTrue(slimeBoots.is(TinkerTags.Items.BOOTS), "Slime boots must be tagged as boots");
        ToolStack bootsTool = ToolStack.from(slimeBoots);
        helper.assertTrue(bootsTool.getMaterials().size() == 2, "Slime boots must have laces and slime material entries; materials=" + bootsTool.getMaterials());
        helper.assertTrue(MaterialIds.skySlimeskin.matchesVariant(bootsTool.getMaterials().get(0).getVariant()),
            "Slime boots laces material must be sky slimeskin; materials=" + bootsTool.getMaterials());
        helper.assertTrue(MaterialIds.blood.matchesVariant(bootsTool.getMaterials().get(1).getVariant()),
            "Slime boots slime material must be blood; materials=" + bootsTool.getMaterials());
        helper.assertTrue(bootsTool.getStats().get(ToolStats.DURABILITY) > 0,
            "Slime boots must expose positive durability from slime stats; stats=" + bootsTool.getStats());
        helper.assertTrue(bootsTool.getFreeSlots(SlotType.UPGRADE) == 5,
            "Slime boots must keep upstream upgrade slots; freeUpgrades=" + bootsTool.getFreeSlots(SlotType.UPGRADE));
        helper.assertTrue(bootsTool.getFreeSlots(SlotType.ABILITY) == 1,
            "Slime boots must keep upstream ability slots; freeAbilities=" + bootsTool.getFreeSlots(SlotType.ABILITY));
        helper.assertTrue(bootsTool.getModifiers().getLevel(ModifierIds.bouncy) == 1,
            "Slime boots must keep the built-in bouncy trait; modifiers=" + bootsTool.getModifiers());
        helper.assertTrue(bootsTool.tryValidate() == null, "Slime boots built from slime materials must validate; error=" + bootsTool.tryValidate());
        ModifiableArmorItem bootsItem = (ModifiableArmorItem)slimeBoots.getItem();
        helper.assertTrue(bootsItem.getEquipmentSlot() == EquipmentSlot.FEET,
            "Slime boots must equip in the feet slot; slot=" + bootsItem.getEquipmentSlot());

        ItemStack enderStaff = ToolBuildHandler.buildItemFromMaterials(TinkerTools.enderStaff.get(), MaterialNBT.EMPTY);
        helper.assertTrue(enderStaff.is(TinkerTools.enderStaff.get()), "Ender staff build must return the registered staff item; result=" + enderStaff);
        helper.assertTrue(enderStaff.is(TinkerTags.Items.STAFFS), "Ender staff must be tagged as a staff");
        helper.assertTrue(enderStaff.is(TinkerTags.Items.INTERACTABLE_DUAL), "Ender staff must support dual interaction");
        ToolStack staffTool = ToolStack.from(enderStaff);
        helper.assertTrue(staffTool.getMaterials().isEmpty(), "Ender staff is a fixed-stat staff and must not require tool materials; materials=" + staffTool.getMaterials());
        helper.assertTrue(staffTool.getStats().get(ToolStats.PROJECTILE_DAMAGE) >= 3,
            "Ender staff must keep upstream projectile damage for sling force; stats=" + staffTool.getStats());
        helper.assertTrue(staffTool.getFreeSlots(SlotType.UPGRADE) == 3,
            "Ender staff must keep upstream upgrade slots; freeUpgrades=" + staffTool.getFreeSlots(SlotType.UPGRADE));
        helper.assertTrue(staffTool.getFreeSlots(SlotType.DEFENSE) == 1,
            "Ender staff must keep upstream defense slot; freeDefense=" + staffTool.getFreeSlots(SlotType.DEFENSE));
        helper.assertTrue(staffTool.getFreeSlots(SlotType.ABILITY) == 2,
            "Ender staff must keep upstream ability slots; freeAbilities=" + staffTool.getFreeSlots(SlotType.ABILITY));
        helper.assertTrue(staffTool.getModifiers().getLevel(ModifierIds.overslimeFriend) == 1,
            "Ender staff must keep overslime friend trait; modifiers=" + staffTool.getModifiers());
        helper.assertTrue(staffTool.getModifiers().getLevel(ModifierIds.reach) == 2,
            "Ender staff must keep reach II trait; modifiers=" + staffTool.getModifiers());
        helper.assertTrue(staffTool.tryValidate() == null, "Ender staff built from slimewood limb material must validate; error=" + staffTool.tryValidate());

        assertTinkerStationRecipeLoaded(helper, level, "tools/modifiers/ability/springing", "Upstream springing modifier recipe");
        assertTinkerStationRecipeLoaded(helper, level, "tools/modifiers/ability/flinging", "Upstream flinging modifier recipe");
        assertTinkerStationRecipeLoaded(helper, level, "tools/modifiers/ability/bonking", "Upstream bonking modifier recipe");
        assertTinkerStationRecipeLoaded(helper, level, "tools/modifiers/ability/warping", "Upstream warping modifier recipe");

        staffTool.addModifier(ModifierIds.springing, 1);
        ModifierEntry springing = staffTool.getUpgrades().getEntry(ModifierIds.springing);
        helper.assertTrue(springing.getLevel() == 1, "Springing must be present as an active staff upgrade; upgrades=" + staffTool.getUpgrades());

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.setItemInHand(InteractionHand.MAIN_HAND, staffTool.createStack());
        player.setOnGround(true);
        player.setDeltaMovement(Vec3.ZERO);
        InteractionResult springingUseResult = springing.getHook(ModifierHooks.GENERAL_INTERACT)
            .onToolUse(staffTool, springing, player, InteractionHand.MAIN_HAND, InteractionSource.RIGHT_CLICK);
        helper.assertTrue(springingUseResult == InteractionResult.SUCCESS,
            "Springing sling right-click must start held charging; result=" + springingUseResult);
        helper.assertTrue(player.isUsingItem(), "Springing sling right-click must put the staff into active use");
        helper.assertTrue(player.getUseItem().is(TinkerTools.enderStaff.get()),
            "Springing sling active use item must be the ender staff; useItem=" + player.getUseItem());
        helper.assertTrue(springing.getHook(ModifierHooks.GENERAL_INTERACT).getUseAction(staffTool, springing) == UseAnim.BOW,
            "Springing sling must expose bow charging animation");
        helper.assertTrue(springing.getHook(ModifierHooks.GENERAL_INTERACT).getUseDuration(staffTool, springing) == 72000,
            "Springing sling held use duration must match upstream charged-use duration");
        int beforeDamage = staffTool.getDamage();
        springing.getHook(ModifierHooks.TOOL_USING).beforeReleaseUsing(staffTool, springing, player, 72000, 71980, springing);
        Vec3 motion = player.getDeltaMovement();
        helper.assertTrue(motion.lengthSqr() > 0.01,
            "Springing sling release must push the player; motion=" + motion);
        helper.assertTrue(motion.y > 0,
            "Springing sling release must include upward motion; motion=" + motion);
        helper.assertTrue(player.getCooldowns().isOnCooldown(staffTool.getItem()),
            "Springing sling release must add a short cooldown to the staff item");
        helper.assertTrue(staffTool.getDamage() == beforeDamage + 1,
            "Springing sling release must damage the staff by one; before=" + beforeDamage + ", after=" + staffTool.getDamage());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void warpingStaffTeleportsServerPlayer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ToolStack staffTool = ToolStack.from(ToolBuildHandler.buildItemFromMaterials(TinkerTools.enderStaff.get(), MaterialNBT.EMPTY));
        staffTool.addModifier(ModifierIds.warping, 1);
        ModifierEntry warping = staffTool.getUpgrades().getEntry(ModifierIds.warping);
        helper.assertTrue(warping.getLevel() == 1, "Warping must be present as an active staff upgrade; upgrades=" + staffTool.getUpgrades());

        BlockPos start = helper.absolutePos(new BlockPos(2, 4, 2));
        for (int z = 0; z <= 8; z++) {
            for (int y = 0; y <= 2; y++) {
                level.setBlock(start.offset(0, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        var player = helper.makeMockServerPlayerInLevel();
        player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, staffTool.createStack());
        Vec3 before = player.position();

        warping.getHook(ModifierHooks.TOOL_USING).beforeReleaseUsing(staffTool, warping, player, 72000, 71900, warping);

        Vec3 after = player.position();
        helper.assertTrue(after.distanceToSqr(before) > 0.25,
            "Warping sling release must teleport the server player; before=" + before + ", after=" + after);
        helper.assertTrue(player.getCooldowns().isOnCooldown(staffTool.getItem()),
            "Warping sling release must add a short cooldown to the staff item");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void flingingStaffPushesPlayerBackwardFromTargetedBlock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ToolStack staffTool = ToolStack.from(ToolBuildHandler.buildItemFromMaterials(TinkerTools.enderStaff.get(), MaterialNBT.EMPTY));
        staffTool.addModifier(ModifierIds.flinging, 1);
        ModifierEntry flinging = staffTool.getUpgrades().getEntry(ModifierIds.flinging);
        helper.assertTrue(flinging.getLevel() == 1, "Flinging must be present as an active staff upgrade; upgrades=" + staffTool.getUpgrades());

        BlockPos start = helper.absolutePos(new BlockPos(2, 4, 2));
        for (int z = -4; z <= 5; z++) {
            for (int y = -1; y <= 2; y++) {
                level.setBlock(start.offset(0, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        level.setBlock(start.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(start.offset(0, 1, 4), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, staffTool.createStack());
        player.setOnGround(true);
        player.setDeltaMovement(Vec3.ZERO);
        int beforeDamage = staffTool.getDamage();

        flinging.getHook(ModifierHooks.TOOL_USING).beforeReleaseUsing(staffTool, flinging, player, 72000, 71900, flinging);

        Vec3 motion = player.getDeltaMovement();
        helper.assertTrue(motion.lengthSqr() > 0.01,
            "Flinging sling release must push the player while targeting a block; motion=" + motion);
        helper.assertTrue(motion.z < -0.01,
            "Flinging sling release must push backward from the targeted block; motion=" + motion);
        helper.assertTrue(player.getCooldowns().isOnCooldown(staffTool.getItem()),
            "Flinging sling release must add a short cooldown to the staff item");
        helper.assertTrue(staffTool.getDamage() == beforeDamage + 1,
            "Flinging sling release must damage the staff by one; before=" + beforeDamage + ", after=" + staffTool.getDamage());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void bonkingStaffKnocksBackTargetEntity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ToolStack staffTool = ToolStack.from(ToolBuildHandler.buildItemFromMaterials(TinkerTools.enderStaff.get(), MaterialNBT.EMPTY));
        staffTool.addModifier(ModifierIds.bonking, 1);
        ModifierEntry bonking = staffTool.getUpgrades().getEntry(ModifierIds.bonking);
        helper.assertTrue(bonking.getLevel() == 1, "Bonking must be present as an active staff upgrade; upgrades=" + staffTool.getUpgrades());

        BlockPos start = helper.absolutePos(new BlockPos(2, 4, 2));
        for (int z = -1; z <= 5; z++) {
            for (int y = -1; y <= 3; y++) {
                level.setBlock(start.offset(0, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        level.setBlock(start.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        TerracubeEntity target = TinkerWorld.terracubeEntity.get().create(level);
        helper.assertTrue(target != null, "Terracube entity type must create a bonking target");
        target.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 3.0, 180, 0);
        target.setNoAi(true);
        target.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(target);

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, staffTool.createStack());
        player.setDeltaMovement(Vec3.ZERO);
        int beforeDamage = staffTool.getDamage();

        bonking.getHook(ModifierHooks.TOOL_USING).beforeReleaseUsing(staffTool, bonking, player, 72000, 71900, bonking);

        Vec3 targetMotion = target.getDeltaMovement();
        helper.assertTrue(targetMotion.lengthSqr() > 0.01,
            "Bonking sling release must knock back the targeted entity; motion=" + targetMotion);
        helper.assertTrue(targetMotion.z > 0.01,
            "Bonking sling release must knock the target away from the player; motion=" + targetMotion);
        helper.assertTrue(player.getCooldowns().isOnCooldown(staffTool.getItem()),
            "Bonking sling release must add a short cooldown to the staff item");
        helper.assertTrue(staffTool.getDamage() == beforeDamage + 1,
            "Bonking sling release must damage the staff by one; before=" + beforeDamage + ", after=" + staffTool.getDamage());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void grappleFishingRodPullsOwnerFromHook(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertTinkerStationRecipeLoaded(helper, level, "tools/modifiers/ability/grapple", "Upstream grapple modifier recipe");

        ItemStack fishingRod = ToolBuildHandler.buildItemFromMaterials(TinkerTools.fishingRod.get(), MaterialNBT.builder()
            .add(MaterialIds.wood)
            .add(MaterialIds.string)
            .add(MaterialIds.flint)
            .build());
        helper.assertTrue(fishingRod.is(TinkerTools.fishingRod.get()), "Fishing rod build must return the registered fishing rod item; result=" + fishingRod);
        helper.assertTrue(fishingRod.is(TinkerTags.Items.FISHING_RODS), "Fishing rod must be tagged as a modifiable fishing rod");

        ToolStack rodTool = ToolStack.from(fishingRod);
        helper.assertTrue(rodTool.getMaterials().size() == 3, "Fishing rod must have limb, string, and hook materials; materials=" + rodTool.getMaterials());
        helper.assertTrue(rodTool.getModifiers().getLevel(ModifierIds.fishing) == 1,
            "Fishing rod must keep the built-in fishing trait; modifiers=" + rodTool.getModifiers());
        rodTool.addModifier(ModifierIds.grapple, 1);
        ModifierEntry grapple = rodTool.getUpgrades().getEntry(ModifierIds.grapple);
        helper.assertTrue(grapple.getLevel() == 1, "Grapple must be present as an active fishing rod upgrade; upgrades=" + rodTool.getUpgrades());
        helper.assertTrue(ModifierUtil.canPerformAction(rodTool, TinkerToolActions.GRAPPLE_HOOK),
            "Grapple fishing rod must expose the grapple hook tool action");

        BlockPos start = helper.absolutePos(new BlockPos(2, 4, 2));
        for (int z = 0; z <= 5; z++) {
            for (int y = -1; y <= 2; y++) {
                level.setBlock(start.offset(0, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        level.setBlock(start.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
        player.setDeltaMovement(Vec3.ZERO);
        ItemStack rodStack = rodTool.createStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, rodStack);

        CombatFishingHook hook = new CombatFishingHook(player, level, 0, 0, 1.0f, 0);
        hook.setGrapple(CombatFishingHook.GrappleType.DASH);
        hook.setPos(start.getX() + 0.5, start.getY() + 1.0, start.getZ() + 4.5);
        hook.setOnGround(true);

        int retrieveDamage = hook.retrieve(rodStack);
        Vec3 motion = player.getDeltaMovement();
        helper.assertTrue(retrieveDamage >= 2 && retrieveDamage <= 3,
            "Grapple retrieve from a stuck hook must report bounded rod damage; damage=" + retrieveDamage);
        helper.assertTrue(motion.lengthSqr() > 0.01,
            "Grapple retrieve must pull the owner toward the hook; motion=" + motion);
        helper.assertTrue(motion.z > 0.01,
            "Grapple retrieve must pull the owner toward the forward hook position; motion=" + motion);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void collectingFishingRodPullCollectsItemEntity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertTinkerStationRecipeLoaded(helper, level, "tools/modifiers/upgrade/collecting", "Upstream collecting modifier recipe");

        ToolStack rodTool = ToolStack.from(buildBasicFishingRod());
        rodTool.addModifier(ModifierIds.collecting, 1);
        ModifierEntry collecting = rodTool.getUpgrades().getEntry(ModifierIds.collecting);
        helper.assertTrue(collecting.getLevel() == 1, "Collecting must be present as an active fishing rod upgrade; upgrades=" + rodTool.getUpgrades());
        helper.assertTrue(ModifierUtil.canPerformAction(rodTool, TinkerToolActions.ITEM_HOOK),
            "Collecting fishing rod must expose the item hook tool action");

        BlockPos start = helper.absolutePos(new BlockPos(2, 4, 2));
        var player = helper.makeMockServerPlayerInLevel();
        player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, rodTool.createStack());
        int beforeDiamonds = player.getInventory().countItem(Items.DIAMOND);

        ItemEntity item = new ItemEntity(level, start.getX() + 0.5, start.getY(), start.getZ() + 2.5, new ItemStack(Items.DIAMOND));
        item.setPickUpDelay(0);
        level.addFreshEntity(item);

        TestCombatFishingHook hook = new TestCombatFishingHook(player, level);
        hook.setCollecting();
        hook.setPos(start.getX() + 0.5, start.getY(), start.getZ() + 2.0);

        helper.assertTrue(hook.exposesCanHitEntity(item), "Collecting hook must be able to hit collectable item entities");
        hook.exposePullEntity(item);

        helper.assertTrue(item.isRemoved(), "Collecting hook must remove the picked up item entity");
        helper.assertTrue(player.getInventory().countItem(Items.DIAMOND) == beforeDiamonds + 1,
            "Collecting hook must transfer the item into the owner's inventory");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void drillAttackFishingRodPullsOwnerOnEntityHook(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertTinkerStationRecipeLoaded(helper, level, "tools/modifiers/ability/drill_attack", "Upstream drill attack modifier recipe");
        helper.assertTrue(ModifierManager.isInTag(ModifierIds.grapple, TinkerTags.Modifiers.DRILL_ATTACKS),
            "Grapple must be tagged as a valid drill attack base modifier");

        ToolStack rodTool = ToolStack.from(buildBasicFishingRod());
        rodTool.addModifier(ModifierIds.grapple, 1);
        rodTool.addModifier(ModifierIds.drillAttack, 1);
        helper.assertTrue(ModifierUtil.canPerformAction(rodTool, TinkerToolActions.GRAPPLE_HOOK),
            "Drill fishing rod must keep the grapple hook tool action");
        helper.assertTrue(ModifierUtil.canPerformAction(rodTool, TinkerToolActions.DRILL_ATTACK),
            "Drill fishing rod must expose the drill attack tool action");

        BlockPos start = helper.absolutePos(new BlockPos(2, 4, 2));
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, rodTool.createStack());
        player.setDeltaMovement(Vec3.ZERO);

        TerracubeEntity target = TinkerWorld.terracubeEntity.get().create(level);
        helper.assertTrue(target != null, "Terracube entity type must create a drill fishing target");
        target.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 4.5, 180, 0);
        target.setNoAi(true);
        target.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(target);

        TestCombatFishingHook hook = new TestCombatFishingHook(player, level);
        hook.setGrapple(CombatFishingHook.GrappleType.DRILL);
        hook.setPos(start.getX() + 0.5, start.getY(), start.getZ() + 3.5);

        hook.exposePullEntity(target);

        Vec3 playerMotion = player.getDeltaMovement();
        Vec3 targetMotion = target.getDeltaMovement();
        helper.assertTrue(playerMotion.lengthSqr() > 0.01,
            "Drill hook entity hit must pull the owner toward the hooked target; motion=" + playerMotion);
        helper.assertTrue(playerMotion.z > 0.01,
            "Drill hook entity hit must pull the owner forward toward the target; motion=" + playerMotion);
        helper.assertTrue(targetMotion.lengthSqr() > 0.01,
            "Fishing hook hit must still pull the target toward the owner; target motion=" + targetMotion);
        helper.assertTrue(targetMotion.z < -0.01,
            "Fishing hook hit must pull the target back toward the owner; target motion=" + targetMotion);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void rangedLongbowAndArrowCreateMaterialProjectile(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, TinkerTables.tinkerStation.get().defaultBlockState(), Block.UPDATE_ALL);

        TinkerStationBlockEntity station = (TinkerStationBlockEntity) level.getBlockEntity(pos);
        helper.assertTrue(station != null, "Tinker station must create a block entity for ranged ammo crafting");
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.TINKER_STATION.get()).stream()
                .anyMatch(holder -> holder.id().equals(ResourceLocation.fromNamespaceAndPath("tconstruct", "tools/building/longbow"))),
            "Upstream longbow tool building recipe must be loaded");
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.TINKER_STATION.get()).stream()
                .anyMatch(holder -> holder.id().equals(ResourceLocation.fromNamespaceAndPath("tconstruct", "tools/building/arrow"))),
            "Upstream arrow tool building recipe must be loaded");

        ItemStack longbow = ToolBuildHandler.buildItemFromMaterials(TinkerTools.longbow.get(), MaterialNBT.builder()
            .add(MaterialIds.wood)
            .add(MaterialIds.wood)
            .add(MaterialIds.wood)
            .add(MaterialIds.string)
            .build());
        helper.assertTrue(longbow.is(TinkerTools.longbow.get()), "Longbow build must return the registered longbow item; result=" + longbow);
        helper.assertTrue(longbow.is(TinkerTags.Items.RANGED), "Longbow must be tagged as a ranged modifiable tool");
        helper.assertTrue(longbow.is(TinkerTags.Items.BOWS), "Longbow must be tagged as a bow");
        ToolStack bowTool = ToolStack.from(longbow);
        helper.assertTrue(bowTool.getMaterials().size() == 4, "Longbow must have two limbs, grip, and bowstring materials; materials=" + bowTool.getMaterials());
        helper.assertTrue(bowTool.getStats().get(ToolStats.DURABILITY) > 0, "Longbow must expose positive durability; stats=" + bowTool.getStats());
        helper.assertTrue(bowTool.getStats().get(ToolStats.DRAW_SPEED) > 0, "Longbow must expose positive draw speed; stats=" + bowTool.getStats());
        helper.assertTrue(bowTool.getStats().get(ToolStats.VELOCITY) > 0, "Longbow must expose positive projectile velocity; stats=" + bowTool.getStats());
        helper.assertTrue(bowTool.tryValidate() == null, "Longbow built from ranged materials must validate; error=" + bowTool.tryValidate());

        station.setItem(TinkerStationBlockEntity.INPUT_SLOT, TinkerToolParts.arrowHead.get().withMaterial(MaterialIds.flint));
        station.setItem(TinkerStationBlockEntity.INPUT_SLOT + 1, TinkerToolParts.arrowShaft.get().withMaterial(MaterialIds.wood));
        station.setItem(TinkerStationBlockEntity.INPUT_SLOT + 2, TinkerToolParts.fletching.get().withMaterial(MaterialIds.feather));

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        ItemStack arrows = station.getCraftingResult().calcResult(player);
        helper.assertTrue(arrows.is(TinkerTools.arrow.get()), "Arrow build must return the registered material arrow item; result=" + arrows);
        helper.assertTrue(arrows.getCount() == 8,
            "Arrow build must apply upstream result_count=4 plus wood shaft economical craft-count trait; count=" + arrows.getCount());
        helper.assertTrue(arrows.is(TinkerTags.Items.AMMO), "Material arrows must be tagged as ammo");
        ToolStack arrowTool = ToolStack.from(arrows);
        helper.assertTrue(arrowTool.getMaterials().size() == 3, "Material arrows must have head, shaft, and fletching materials; materials=" + arrowTool.getMaterials());
        helper.assertTrue(MaterialIds.flint.matchesVariant(arrowTool.getMaterials().get(0).getVariant()),
            "Arrow head material must be flint; materials=" + arrowTool.getMaterials());
        helper.assertTrue(MaterialIds.wood.matchesVariant(arrowTool.getMaterials().get(1).getVariant()),
            "Arrow shaft material must be wood; materials=" + arrowTool.getMaterials());
        helper.assertTrue(MaterialIds.feather.matchesVariant(arrowTool.getMaterials().get(2).getVariant()),
            "Arrow fletching material must be feather; materials=" + arrowTool.getMaterials());
        helper.assertTrue(arrowTool.getStats().get(ToolStats.PROJECTILE_DAMAGE) > 0,
            "Material arrows must expose positive projectile damage; stats=" + arrowTool.getStats());
        helper.assertTrue(arrowTool.getStats().get(ToolStats.VELOCITY) > 0,
            "Material arrows must expose positive projectile velocity; stats=" + arrowTool.getStats());
        helper.assertTrue(arrowTool.getModifiers().getLevel(ModifierIds.economical) == 1,
            "Wood arrow shaft must add the economical trait that doubles crafted ammo; modifiers=" + arrowTool.getModifiers());
        helper.assertTrue(arrowTool.tryValidate() == null, "Material arrows built from ammo materials must validate; error=" + arrowTool.tryValidate());

        AbstractArrow projectile = ((ModifiableArrowItem) arrows.getItem()).createArrow(level, arrows, player, longbow);
        helper.assertTrue(projectile instanceof ModifiableArrow, "Material arrow item must create a modifiable arrow projectile; projectile=" + projectile);
        helper.assertTrue(projectile.getType() == TinkerTools.materialArrow.get(),
            "Material arrow projectile must use the registered tconstruct arrow entity type; type=" + projectile.getType());
        helper.assertTrue(projectile.getOwner() == player, "Material arrow projectile must keep the shooter as owner");
        helper.assertTrue(projectile.getBaseDamage() > 0, "Material arrow projectile must copy positive projectile damage; damage=" + projectile.getBaseDamage());
        helper.assertTrue(((ModifiableArrow) projectile).getPickupItem().is(TinkerTools.arrow.get()),
            "Material arrow projectile pickup stack must remain a TConstruct arrow; pickup=" + ((ModifiableArrow) projectile).getPickupItem());

        station.getCraftingResult().craftResult(player, arrows.copy(), arrows.getCount());
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.INPUT_SLOT).isEmpty(),
            "Arrow crafting must consume the arrow head input");
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.INPUT_SLOT + 1).isEmpty(),
            "Arrow crafting must consume the arrow shaft input");
        helper.assertTrue(station.getItem(TinkerStationBlockEntity.INPUT_SLOT + 2).isEmpty(),
            "Arrow crafting must consume the fletching input");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void punjiBlockPlacementConnectionsAndDamageBehavior(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockState support = Blocks.STONE.defaultBlockState();
        BlockState punji = TinkerGadgets.punji.get().defaultBlockState().setValue(PunjiBlock.FACING, Direction.DOWN);

        for (BlockPos offset : List.of(BlockPos.ZERO, new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(1, 0, -1), new BlockPos(-1, 0, -1))) {
            level.setBlock(center.offset(offset).below(), support, Block.UPDATE_ALL);
            if (!offset.equals(BlockPos.ZERO)) {
                level.setBlock(center.offset(offset), punji, Block.UPDATE_ALL);
            }
        }

        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TinkerGadgets.punji.get()));
        BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND),
            new BlockHitResult(Vec3.atCenterOf(center.below()), Direction.UP, center.below(), false));
        BlockState placed = TinkerGadgets.punji.get().getStateForPlacement(context);

        helper.assertTrue(placed != null, "Punji must be placeable on a sturdy block face");
        helper.assertTrue(placed.getValue(PunjiBlock.FACING) == Direction.DOWN, "Punji placed on top of a block must face down; state=" + placed);
        helper.assertTrue(placed.getValue(BlockStateProperties.NORTH), "Punji must connect to matching north neighbor; state=" + placed);
        helper.assertTrue(placed.getValue(BlockStateProperties.EAST), "Punji must connect to matching east neighbor; state=" + placed);
        helper.assertTrue(placed.getBlock().getBlockPathType(placed, level, center, null) == PathType.DAMAGE_OTHER,
            "Punji must expose the dangerous path type to mob pathfinding");
        helper.assertTrue(!punji.canSurvive(level, center.offset(2, 0, 0)),
            "Punji without support must not survive on its target face");

        BlockPos waterPos = helper.absolutePos(new BlockPos(7, 2, 3));
        level.setBlock(waterPos.below(), support, Block.UPDATE_ALL);
        level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        BlockPlaceContext waterContext = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND),
            new BlockHitResult(Vec3.atCenterOf(waterPos.below()), Direction.UP, waterPos.below(), false));
        BlockState waterlogged = TinkerGadgets.punji.get().getStateForPlacement(waterContext);
        helper.assertTrue(waterlogged != null && waterlogged.getValue(BlockStateProperties.WATERLOGGED),
            "Punji placed into water must preserve waterlogging; state=" + waterlogged);
        helper.assertTrue(waterlogged != null && waterlogged.getFluidState().getType() == Fluids.WATER,
            "Waterlogged punji must expose a water fluid state; state=" + waterlogged.getFluidState());

        level.setBlock(center, placed, Block.UPDATE_ALL);
        SkySlimeEntity target = TinkerWorld.skySlimeEntity.get().create(level);
        helper.assertTrue(target != null, "Punji damage test requires a sky slime target");
        target.setPos(center.getX() + 0.5D, center.getY() + 0.1D, center.getZ() + 0.5D);
        level.addFreshEntity(target);

        float health = target.getHealth();
        ((PunjiBlock)placed.getBlock()).entityInside(placed, level, center, target);
        helper.assertTrue(target.getHealth() < health, "Punji must damage living entities in the pointed half of the block");

        float damagedHealth = target.getHealth();
        target.invulnerableTime = 0;
        target.setPos(center.getX() + 0.5D, center.getY() + 0.9D, center.getZ() + 0.5D);
        ((PunjiBlock)placed.getBlock()).entityInside(placed, level, center, target);
        helper.assertTrue(target.getHealth() == damagedHealth,
            "Down-facing punji must not damage entities outside the pointed half; before=" + damagedHealth + ", after=" + target.getHealth());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void piggyBackpackCarriesAndTrimsEntityStack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockServerPlayerInLevel();
        player.setPos(helper.absolutePos(new BlockPos(4, 2, 4)).getCenter());
        ItemStack backpackStack = new ItemStack(TinkerGadgets.piggyBackpack.get(), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, backpackStack);

        SkySlimeEntity first = TinkerWorld.skySlimeEntity.get().create(level);
        SkySlimeEntity second = TinkerWorld.skySlimeEntity.get().create(level);
        helper.assertTrue(first != null, "Piggyback test requires a first sky slime target");
        helper.assertTrue(second != null, "Piggyback test requires a second sky slime target");
        first.setPos(player.getX() + 1, player.getY(), player.getZ());
        second.setPos(player.getX() + 2, player.getY(), player.getZ());
        level.addFreshEntity(first);
        level.addFreshEntity(second);

        InteractionResult firstResult = TinkerGadgets.piggyBackpack.get().interactLivingEntity(backpackStack, player, first, InteractionHand.MAIN_HAND);
        ItemStack chestBackpack = player.getItemBySlot(EquipmentSlot.CHEST);
        helper.assertTrue(firstResult == InteractionResult.SUCCESS, "Piggyback backpack must pick up the first valid living target; result=" + firstResult);
        helper.assertTrue(first.getVehicle() == player, "First carried entity must ride the player");
        helper.assertTrue(player.getPassengers().contains(first), "Player must list the first carried entity as a passenger");
        helper.assertTrue(chestBackpack.is(TinkerGadgets.piggyBackpack.get()) && chestBackpack.getCount() == 1,
            "First pickup must equip exactly one piggy backpack in the chest slot; chest=" + chestBackpack);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 2,
            "First pickup must consume one backpack from the hand stack; hand=" + player.getItemInHand(InteractionHand.MAIN_HAND));

        InteractionResult secondResult = TinkerGadgets.piggyBackpack.get().interactLivingEntity(player.getItemInHand(InteractionHand.MAIN_HAND), player, second, InteractionHand.MAIN_HAND);
        helper.assertTrue(secondResult == InteractionResult.SUCCESS, "Piggyback backpack must pick up a second valid living target; result=" + secondResult);
        helper.assertTrue(second.getVehicle() == first, "Second carried entity must ride the top carried entity");
        helper.assertTrue(first.getPassengers().contains(second), "First carried entity must list the second carried entity as a passenger");
        helper.assertTrue(chestBackpack.getCount() == 2, "Second pickup must grow equipped backpack count to match carried entities; chest=" + chestBackpack);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
            "Second pickup must consume one more backpack from the hand stack; hand=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        helper.assertTrue(player.getCapability(PiggybackCapability.PIGGYBACK) != null,
            "Server players must expose the piggyback entity capability");

        TinkerGadgets.piggyBackpack.get().inventoryTick(chestBackpack, level, player, EquipmentSlot.CHEST.getIndex(), false);
        MobEffectInstance carrying = player.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TinkerGadgets.carryEffect.get()));
        helper.assertTrue(carrying != null && carrying.getAmplifier() == 1,
            "Carrying two entities must apply the carry effect with amplifier 1; effect=" + carrying);

        chestBackpack.setCount(1);
        TinkerGadgets.carryEffect.get().applyEffectTick(player, carrying.getAmplifier());
        helper.assertTrue(first.getVehicle() == player, "Reducing equipped backpack count to one must keep the first carried entity");
        helper.assertTrue(second.getVehicle() == null, "Reducing equipped backpack count to one must drop extra carried entities");

        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        TinkerGadgets.carryEffect.get().applyEffectTick(player, 0);
        helper.assertTrue(first.getVehicle() == null, "Removing the piggyback backpack must drop the remaining carried entity");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void fancyItemFramesKeepVariantPlacementRotationAndImmunity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockServerPlayerInLevel();
        BlockPos wall = helper.absolutePos(new BlockPos(4, 3, 4));
        Direction face = Direction.NORTH;
        BlockPos framePos = wall.relative(face);
        level.setBlock(wall, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        ItemStack diamondFrameStack = new ItemStack(TinkerGadgets.itemFrame.get(FrameType.DIAMOND), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, diamondFrameStack);
        InteractionResult placeResult = diamondFrameStack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(wall), face, wall, false)));
        List<FancyItemFrameEntity> placedFrames = level.getEntitiesOfClass(FancyItemFrameEntity.class, new AABB(framePos).inflate(1));
        helper.assertTrue(placeResult.consumesAction(), "Fancy item frame item use must consume the placement action; result=" + placeResult);
        helper.assertTrue(diamondFrameStack.getCount() == 1, "Fancy item frame placement must shrink the hand stack; stack=" + diamondFrameStack);
        helper.assertTrue(placedFrames.size() == 1, "Fancy item frame placement must spawn exactly one frame entity; frames=" + placedFrames);
        FancyItemFrameEntity placedDiamond = placedFrames.get(0);
        helper.assertTrue(placedDiamond.getFrameType() == FrameType.DIAMOND, "Placed diamond frame must preserve its variant; type=" + placedDiamond.getFrameType());
        helper.assertTrue(placedDiamond.getFrameItem() == TinkerGadgets.itemFrame.get(FrameType.DIAMOND),
            "Placed diamond frame must drop/pick as its matching frame item");
        helper.assertTrue(placedDiamond.getDirection() == face, "Placed frame must face the clicked side; direction=" + placedDiamond.getDirection());

        placedDiamond.setItem(new ItemStack(Items.DIAMOND), true);
        setFancyFrameRotation(placedDiamond, 20);
        helper.assertTrue(placedDiamond.getRotation() == 16,
            "Diamond fancy frames must cap at 16 rotations instead of wrapping at 8; rotation=" + placedDiamond.getRotation());
        helper.assertTrue(placedDiamond.getAnalogOutput() == 16,
            "Diamond fancy frame comparator output must expose the 16-step rotation value; output=" + placedDiamond.getAnalogOutput());
        helper.assertTrue(placedDiamond.getPickedResult(null).is(Items.DIAMOND),
            "Picking a non-empty fancy frame must return the displayed item");

        FancyItemFrameEntity gold = makeFancyFrame(level, helper.absolutePos(new BlockPos(7, 3, 4)), Direction.NORTH, FrameType.GOLD);
        gold.setItem(new ItemStack(Items.GOLD_INGOT), true);
        tickEntity(gold, 20);
        helper.assertTrue(gold.getRotation() == 1, "Gold fancy frames must rotate forward once per second; rotation=" + gold.getRotation());
        helper.assertTrue(gold.getAnalogOutput() == 2, "Gold fancy frame comparator output must follow vanilla 8-step output; output=" + gold.getAnalogOutput());

        FancyItemFrameEntity reversedGold = makeFancyFrame(level, helper.absolutePos(new BlockPos(9, 3, 4)), Direction.NORTH, FrameType.REVERSED_GOLD);
        reversedGold.setItem(new ItemStack(Items.GOLD_INGOT), true);
        tickEntity(reversedGold, 20);
        helper.assertTrue(reversedGold.getRotation() == 7,
            "Reversed gold fancy frames must rotate backward once per second; rotation=" + reversedGold.getRotation());
        helper.assertTrue(reversedGold.getAnalogOutput() == 8,
            "Reversed gold fancy frame comparator output must reflect the wrapped backward rotation; output=" + reversedGold.getAnalogOutput());

        FancyItemFrameEntity netherite = makeFancyFrame(level, helper.absolutePos(new BlockPos(11, 3, 4)), Direction.NORTH, FrameType.NETHERITE);
        helper.assertTrue(netherite.fireImmune(), "Netherite fancy frames must keep upstream fire immunity");
        helper.assertTrue(netherite.getPickedResult(null).is(TinkerGadgets.itemFrame.get(FrameType.NETHERITE)),
            "Picking an empty netherite fancy frame must return the matching frame item");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void slimeFoodCakesApplyEffectsAndRespectCombinationRules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        BlockPos skyPos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(skyPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(skyPos, TinkerGadgets.cake.get(FoliageType.SKY).defaultBlockState(), Block.UPDATE_ALL);
        InteractionResult skyResult = level.getBlockState(skyPos).useWithoutItem(level, player,
            new BlockHitResult(Vec3.atCenterOf(skyPos), Direction.UP, skyPos, false));
        MobEffectInstance doubleJump = player.getEffect(TinkerEffects.holder(TinkerEffects.doubleJump));
        helper.assertTrue(skyResult == InteractionResult.SUCCESS, "Sky slime cake must be edible with an empty hand; result=" + skyResult);
        helper.assertTrue(level.getBlockState(skyPos).getValue(CakeBlock.BITES) == 1,
            "Eating one sky cake slice must increment cake bites; state=" + level.getBlockState(skyPos));
        helper.assertTrue(doubleJump != null && doubleJump.getDuration() > 500,
            "Sky slime cake must apply the upstream double-jump effect; effect=" + doubleJump);

        InteractionResult blockedSkyResult = level.getBlockState(skyPos).useWithoutItem(level, player,
            new BlockHitResult(Vec3.atCenterOf(skyPos), Direction.UP, skyPos, false));
        helper.assertTrue(blockedSkyResult == InteractionResult.PASS,
            "BLOCK-combination slime cakes must not be eaten again while their effect is still active; result=" + blockedSkyResult);
        helper.assertTrue(level.getBlockState(skyPos).getValue(CakeBlock.BITES) == 1,
            "Blocked second sky cake use must not consume another slice; state=" + level.getBlockState(skyPos));

        BlockPos enderPos = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlock(enderPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(enderPos, TinkerGadgets.cake.get(FoliageType.ENDER).defaultBlockState(), Block.UPDATE_ALL);
        InteractionResult firstEnderResult = level.getBlockState(enderPos).useWithoutItem(level, player,
            new BlockHitResult(Vec3.atCenterOf(enderPos), Direction.UP, enderPos, false));
        MobEffectInstance firstReturning = player.getEffect(TinkerEffects.holder(TinkerEffects.returning));
        helper.assertTrue(firstEnderResult == InteractionResult.SUCCESS, "Ender slime cake first bite must succeed; result=" + firstEnderResult);
        helper.assertTrue(firstReturning != null, "Ender slime cake must apply the returning effect");
        int firstReturningDuration = firstReturning.getDuration();
        InteractionResult secondEnderResult = level.getBlockState(enderPos).useWithoutItem(level, player,
            new BlockHitResult(Vec3.atCenterOf(enderPos), Direction.UP, enderPos, false));
        MobEffectInstance secondReturning = player.getEffect(TinkerEffects.holder(TinkerEffects.returning));
        helper.assertTrue(secondEnderResult == InteractionResult.SUCCESS,
            "ADD-combination ender cake must remain edible while its effect is active; result=" + secondEnderResult);
        helper.assertTrue(level.getBlockState(enderPos).getValue(CakeBlock.BITES) == 2,
            "Eating ender cake twice must consume two slices; state=" + level.getBlockState(enderPos));
        helper.assertTrue(secondReturning != null && secondReturning.getDuration() > firstReturningDuration,
            "ADD-combination ender cake must extend matching effect duration; before=" + firstReturningDuration + ", after=" + secondReturning);

        BlockPos ichorPos = helper.absolutePos(new BlockPos(6, 2, 2));
        level.setBlock(ichorPos.above(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        BlockState ichorCake = TinkerGadgets.cake.get(FoliageType.ICHOR).defaultBlockState();
        level.setBlock(ichorPos, ichorCake, Block.UPDATE_ALL);
        helper.assertTrue(ichorCake.canSurvive(level, ichorPos), "Inverted ichor cake must survive only with a solid block above");
        InteractionResult ichorResult = level.getBlockState(ichorPos).useWithoutItem(level, player,
            new BlockHitResult(Vec3.atCenterOf(ichorPos), Direction.DOWN, ichorPos, false));
        MobEffectInstance antigravity = player.getEffect(TinkerEffects.holder(TinkerEffects.antigravity));
        helper.assertTrue(ichorResult == InteractionResult.SUCCESS, "Inverted ichor cake must be edible from its hanging placement; result=" + ichorResult);
        helper.assertTrue(antigravity != null && antigravity.getDuration() > 500,
            "Ichor cake must apply the upstream antigravity effect; effect=" + antigravity);
        level.removeBlock(ichorPos.above(), false);
        BlockState updatedIchor = level.getBlockState(ichorPos).updateShape(Direction.UP, Blocks.AIR.defaultBlockState(), level, ichorPos, ichorPos.above());
        helper.assertTrue(updatedIchor.isAir(), "Inverted ichor cake must break when its upper support is removed; state=" + updatedIchor);

        BlockPos magmaPos = helper.absolutePos(new BlockPos(8, 2, 2));
        level.setBlock(magmaPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(magmaPos, TinkerGadgets.magmaCake.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(ComposterBlock.COMPOSTABLES.getFloat(TinkerGadgets.magmaCake.get().asItem()) == 1.0F,
            "Magma cake must keep upstream full composting chance");
        InteractionResult magmaResult = level.getBlockState(magmaPos).useWithoutItem(level, player,
            new BlockHitResult(Vec3.atCenterOf(magmaPos), Direction.UP, magmaPos, false));
        MobEffectInstance fireResistance = player.getEffect(MobEffects.FIRE_RESISTANCE);
        helper.assertTrue(magmaResult == InteractionResult.SUCCESS, "Magma cake must be edible with an empty hand; result=" + magmaResult);
        helper.assertTrue(level.getBlockState(magmaPos).getValue(CakeBlock.BITES) == 1,
            "Eating one magma cake slice must increment cake bites; state=" + level.getBlockState(magmaPos));
        helper.assertTrue(fireResistance != null && fireResistance.getDuration() > 500,
            "Magma cake must apply the upstream fire resistance effect; effect=" + fireResistance);
        InteractionResult blockedMagmaResult = level.getBlockState(magmaPos).useWithoutItem(level, player,
            new BlockHitResult(Vec3.atCenterOf(magmaPos), Direction.UP, magmaPos, false));
        helper.assertTrue(blockedMagmaResult == InteractionResult.PASS,
            "BLOCK-combination magma cake must not be eaten again while fire resistance is active; result=" + blockedMagmaResult);
        helper.assertTrue(level.getBlockState(magmaPos).getValue(CakeBlock.BITES) == 1,
            "Blocked second magma cake use must not consume another slice; state=" + level.getBlockState(magmaPos));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void gadgetThrowablesSpawnTheirProjectiles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TinkerGadgets.efln.get(), 2));
        int eflnBefore = level.getEntitiesOfClass(EFLNEntity.class, player.getBoundingBox().inflate(8)).size();
        TinkerGadgets.efln.get().use(level, player, InteractionHand.MAIN_HAND);
        List<EFLNEntity> eflns = level.getEntitiesOfClass(EFLNEntity.class, player.getBoundingBox().inflate(8));
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
            "EFLN use must consume one item in survival-like mode; remaining=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        helper.assertTrue(eflns.size() == eflnBefore + 1, "EFLN use must spawn one EFLN projectile; before=" + eflnBefore + ", after=" + eflns.size());
        EFLNEntity efln = eflns.get(eflns.size() - 1);
        helper.assertTrue(efln.getType() == TinkerGadgets.eflnEntity.get(), "EFLN projectile must use the registered entity type; type=" + efln.getType());
        helper.assertTrue(efln.getOwner() == player, "EFLN projectile must keep the throwing player as owner");
        helper.assertTrue(efln.getItem().is(TinkerGadgets.efln.get()), "EFLN projectile must carry an EFLN item stack; item=" + efln.getItem());

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TinkerGadgets.glowBall.get(), 2));
        int glowBefore = level.getEntitiesOfClass(GlowballEntity.class, player.getBoundingBox().inflate(8)).size();
        TinkerGadgets.glowBall.get().use(level, player, InteractionHand.MAIN_HAND);
        List<GlowballEntity> glowballs = level.getEntitiesOfClass(GlowballEntity.class, player.getBoundingBox().inflate(8));
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
            "Glow ball use must consume one item in survival-like mode; remaining=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        helper.assertTrue(glowballs.size() == glowBefore + 1,
            "Glow ball use must spawn one glowball projectile; before=" + glowBefore + ", after=" + glowballs.size());
        GlowballEntity glowball = glowballs.get(glowballs.size() - 1);
        helper.assertTrue(glowball.getType() == TinkerGadgets.glowBallEntity.get(),
            "Glow ball projectile must use the registered entity type; type=" + glowball.getType());
        helper.assertTrue(glowball.getOwner() == player, "Glow ball projectile must keep the throwing player as owner");
        helper.assertTrue(glowball.getItem().is(TinkerGadgets.glowBall.get()),
            "Glow ball projectile must carry a glow ball item stack; item=" + glowball.getItem());

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TinkerGadgets.flintShuriken.get(), 2));
        int shurikenBefore = level.getEntitiesOfClass(FlintShurikenEntity.class, player.getBoundingBox().inflate(8)).size();
        TinkerGadgets.flintShuriken.get().use(level, player, InteractionHand.MAIN_HAND);
        List<FlintShurikenEntity> shurikens = level.getEntitiesOfClass(FlintShurikenEntity.class, player.getBoundingBox().inflate(8));
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
            "Flint shuriken use must consume one item in survival-like mode; remaining=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        helper.assertTrue(shurikens.size() == shurikenBefore + 1,
            "Flint shuriken use must spawn one shuriken projectile; before=" + shurikenBefore + ", after=" + shurikens.size());
        FlintShurikenEntity shuriken = shurikens.get(shurikens.size() - 1);
        helper.assertTrue(shuriken.getType() == TinkerGadgets.flintShurikenEntity.get(),
            "Flint shuriken projectile must use the registered entity type; type=" + shuriken.getType());
        helper.assertTrue(shuriken.getOwner() == player, "Flint shuriken projectile must keep the throwing player as owner");
        helper.assertTrue(shuriken.getItem().is(TinkerGadgets.flintShuriken.get()),
            "Flint shuriken projectile must carry a flint shuriken item stack; item=" + shuriken.getItem());
        helper.assertTrue(shuriken.getDamage() == 3.0F, "Flint shuriken projectile damage must match the gadget definition; damage=" + shuriken.getDamage());
        helper.assertTrue(shuriken.getKnockback() == 0.6F, "Flint shuriken projectile knockback must match the gadget definition; knockback=" + shuriken.getKnockback());

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TinkerGadgets.quartzShuriken.get(), 2));
        int quartzBefore = level.getEntitiesOfClass(QuartzShurikenEntity.class, player.getBoundingBox().inflate(8)).size();
        TinkerGadgets.quartzShuriken.get().use(level, player, InteractionHand.MAIN_HAND);
        List<QuartzShurikenEntity> quartzShurikens = level.getEntitiesOfClass(QuartzShurikenEntity.class, player.getBoundingBox().inflate(8));
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
            "Quartz shuriken use must consume one item in survival-like mode; remaining=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        helper.assertTrue(quartzShurikens.size() == quartzBefore + 1,
            "Quartz shuriken use must spawn one shuriken projectile; before=" + quartzBefore + ", after=" + quartzShurikens.size());
        QuartzShurikenEntity quartzShuriken = quartzShurikens.get(quartzShurikens.size() - 1);
        helper.assertTrue(quartzShuriken.getType() == TinkerGadgets.quartzShurikenEntity.get(),
            "Quartz shuriken projectile must use the registered entity type; type=" + quartzShuriken.getType());
        helper.assertTrue(quartzShuriken.getOwner() == player, "Quartz shuriken projectile must keep the throwing player as owner");
        helper.assertTrue(quartzShuriken.getItem().is(TinkerGadgets.quartzShuriken.get()),
            "Quartz shuriken projectile must carry a quartz shuriken item stack; item=" + quartzShuriken.getItem());
        helper.assertTrue(quartzShuriken.getDamage() == 5.0F, "Quartz shuriken projectile damage must match the gadget definition; damage=" + quartzShuriken.getDamage());
        helper.assertTrue(quartzShuriken.getKnockback() == 0.4F, "Quartz shuriken projectile knockback must match the gadget definition; knockback=" + quartzShuriken.getKnockback());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void gadgetThrowablesDispenseTheirProjectiles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        BlockPos eflnPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos glowPos = helper.absolutePos(new BlockPos(3, 2, 1));
        BlockPos flintPos = helper.absolutePos(new BlockPos(5, 2, 1));
        BlockPos quartzPos = helper.absolutePos(new BlockPos(7, 2, 1));

        placeLoadedDispenser(helper, level, eflnPos, new ItemStack(TinkerGadgets.efln.get(), 2));
        placeLoadedDispenser(helper, level, glowPos, new ItemStack(TinkerGadgets.glowBall.get(), 2));
        placeLoadedDispenser(helper, level, flintPos, new ItemStack(TinkerGadgets.flintShuriken.get(), 2));
        placeLoadedDispenser(helper, level, quartzPos, new ItemStack(TinkerGadgets.quartzShuriken.get(), 2));

        int eflnBefore = level.getEntitiesOfClass(EFLNEntity.class, new AABB(eflnPos).inflate(6)).size();
        int glowBefore = level.getEntitiesOfClass(GlowballEntity.class, new AABB(glowPos).inflate(6)).size();
        int flintBefore = level.getEntitiesOfClass(FlintShurikenEntity.class, new AABB(flintPos).inflate(6)).size();
        int quartzBefore = level.getEntitiesOfClass(QuartzShurikenEntity.class, new AABB(quartzPos).inflate(6)).size();

        dispenseLoadedDispenser(level, eflnPos);
        dispenseLoadedDispenser(level, glowPos);
        dispenseLoadedDispenser(level, flintPos);
        dispenseLoadedDispenser(level, quartzPos);

        helper.runAfterDelay(2, () -> {
            List<EFLNEntity> eflns = level.getEntitiesOfClass(EFLNEntity.class, new AABB(eflnPos).inflate(6));
            List<GlowballEntity> glowballs = level.getEntitiesOfClass(GlowballEntity.class, new AABB(glowPos).inflate(6));
            List<FlintShurikenEntity> flints = level.getEntitiesOfClass(FlintShurikenEntity.class, new AABB(flintPos).inflate(6));
            List<QuartzShurikenEntity> quartzes = level.getEntitiesOfClass(QuartzShurikenEntity.class, new AABB(quartzPos).inflate(6));

            helper.assertTrue(eflns.size() == eflnBefore + 1,
                "Dispenser must launch one EFLN projectile through registered behavior; before=" + eflnBefore + ", after=" + eflns.size());
            helper.assertTrue(glowballs.size() == glowBefore + 1,
                "Dispenser must launch one glow ball projectile through registered behavior; before=" + glowBefore + ", after=" + glowballs.size());
            helper.assertTrue(flints.size() == flintBefore + 1,
                "Dispenser must launch one flint shuriken projectile through registered behavior; before=" + flintBefore + ", after=" + flints.size());
            helper.assertTrue(quartzes.size() == quartzBefore + 1,
                "Dispenser must launch one quartz shuriken projectile through registered behavior; before=" + quartzBefore + ", after=" + quartzes.size());

            EFLNEntity efln = eflns.get(eflns.size() - 1);
            GlowballEntity glowball = glowballs.get(glowballs.size() - 1);
            FlintShurikenEntity flint = flints.get(flints.size() - 1);
            QuartzShurikenEntity quartz = quartzes.get(quartzes.size() - 1);

            helper.assertTrue(efln.getOwner() == null && efln.getItem().is(TinkerGadgets.efln.get()),
                "Dispensed EFLN must carry its item stack and have no player owner; owner=" + efln.getOwner() + ", item=" + efln.getItem());
            helper.assertTrue(glowball.getOwner() == null && glowball.getItem().is(TinkerGadgets.glowBall.get()),
                "Dispensed glow ball must carry its item stack and have no player owner; owner=" + glowball.getOwner() + ", item=" + glowball.getItem());
            helper.assertTrue(flint.getOwner() == null && flint.getItem().is(TinkerGadgets.flintShuriken.get()) && flint.getDamage() == 3.0F && flint.getKnockback() == 0.6F,
                "Dispensed flint shuriken must keep item/damage/knockback definitions; item=" + flint.getItem() + ", damage=" + flint.getDamage() + ", knockback=" + flint.getKnockback());
            helper.assertTrue(quartz.getOwner() == null && quartz.getItem().is(TinkerGadgets.quartzShuriken.get()) && quartz.getDamage() == 5.0F && quartz.getKnockback() == 0.4F,
                "Dispensed quartz shuriken must keep item/damage/knockback definitions; item=" + quartz.getItem() + ", damage=" + quartz.getDamage() + ", knockback=" + quartz.getKnockback());

            assertDispenserSlotCount(helper, level, eflnPos, 1, "EFLN dispenser");
            assertDispenserSlotCount(helper, level, glowPos, 1, "Glow ball dispenser");
            assertDispenserSlotCount(helper, level, flintPos, 1, "Flint shuriken dispenser");
            assertDispenserSlotCount(helper, level, quartzPos, 1, "Quartz shuriken dispenser");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 100)
    public static void excavatorBreaksThreeByThreeDirt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(center.offset(x, 0, z), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        ItemStack excavator = buildIronExcavator();
        ToolStack tool = ToolStack.from(excavator);
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, excavator);

        BlockState state = level.getBlockState(center);
        helper.assertTrue(excavator.is(TinkerTags.Items.HARVEST), "Excavator stack must be in the harvest item tag");
        helper.assertTrue(excavator.is(TinkerTags.Items.AOE), "Excavator stack must be in the AOE item tag");
        helper.assertTrue(state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL), "Dirt must be in the vanilla shovel mineable block tag");
        helper.assertTrue(IsEffectiveToolHook.isEffective(tool, state), "Excavator must be effective on dirt before AOE harvest");

        int harvested = ToolHarvestLogic.runBlockBreak(excavator, tool, state, center, Direction.UP, player, null);
        int removed = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (level.getBlockState(center.offset(x, 0, z)).isAir()) {
                    removed++;
                }
            }
        }
        helper.assertTrue(harvested >= 1, "Excavator server harvest must break at least the center block; harvested=" + harvested + ", removed=" + removed);
        helper.assertTrue(removed == 9, "Excavator server harvest must remove the full 3x3 dirt area; harvested=" + harvested + ", removed=" + removed);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 100)
    public static void excavatorBreakEventBreaksThreeByThreeDirt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(center.offset(x, 0, z), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        ItemStack excavator = buildIronExcavator();
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, excavator);

        boolean destroyed = player.gameMode.destroyBlock(center);
        int removed = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (level.getBlockState(center.offset(x, 0, z)).isAir()) {
                    removed++;
                }
            }
        }

        helper.assertTrue(!destroyed || removed == 9, "ServerPlayerGameMode may return false when TConstruct cancels vanilla breaking, but AOE must still apply; destroyed=" + destroyed + ", removed=" + removed);
        helper.assertTrue(removed == 9, "Excavator BreakEvent path must remove the full 3x3 dirt area; removed=" + removed);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 100)
    public static void pickaxeMinesStoneWithStatsAndDurability(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos stonePos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(stonePos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        ItemStack pickaxe = buildIronPickaxe();
        ToolStack tool = ToolStack.from(pickaxe);
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);

        BlockState stone = level.getBlockState(stonePos);
        helper.assertTrue(pickaxe.is(TinkerTags.Items.HARVEST), "Pickaxe stack must be in the harvest item tag");
        helper.assertTrue(pickaxe.is(TinkerTags.Items.HARVEST_PRIMARY), "Pickaxe stack must be in the primary harvest item tag");
        helper.assertTrue(stone.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE), "Stone must be in the vanilla pickaxe mineable block tag");
        helper.assertTrue(pickaxe.getItem().isCorrectToolForDrops(pickaxe, stone), "Iron TConstruct pickaxe must be correct for stone drops");
        helper.assertTrue(IsEffectiveToolHook.isEffective(tool, stone), "Iron TConstruct pickaxe must be effective on stone");
        int expectedDamage = ToolHarvestLogic.getDamage(tool, level, stonePos, stone);
        helper.assertTrue(expectedDamage == 1, "Mining stone with a primary harvest pickaxe must cost one durability; damage=" + expectedDamage);

        float statSpeed = tool.getStats().get(ToolStats.MINING_SPEED);
        float itemSpeed = pickaxe.getItem().getDestroySpeed(pickaxe, stone);
        helper.assertTrue(itemSpeed == statSpeed && itemSpeed > 1.0F,
            "ModifiableItem destroy speed must come from TConstruct mining stats on effective stone; statSpeed=" + statSpeed + ", itemSpeed=" + itemSpeed);

        boolean mined = pickaxe.getItem().mineBlock(pickaxe, level, stone, stonePos, player);
        helper.assertTrue(mined, "ModifiableItem.mineBlock must accept harvest tools");
        ToolStack afterMining = ToolStack.from(pickaxe);
        ToolDamageUtil.damage(afterMining, expectedDamage, null, pickaxe);
        helper.assertTrue(afterMining.getDamage() == expectedDamage,
            "ToolDamageUtil must persist the calculated mining damage into the TConstruct pickaxe NBT; expected=" + expectedDamage + ", after=" + afterMining.getDamage());

        ItemStack brokenPickaxe = pickaxe.copy();
        ToolDamageUtil.breakTool(brokenPickaxe);
        helper.assertTrue(ToolDamageUtil.isBroken(brokenPickaxe), "Raw ToolDamageUtil.breakTool must persist broken state into CustomData");
        ToolStack brokenTool = ToolStack.from(brokenPickaxe);
        helper.assertTrue(brokenTool.isBroken(), "ToolStack must read the raw broken flag persisted on the pickaxe stack");
        helper.assertTrue(IsEffectiveToolHook.isEffective(brokenTool, stone) == false, "Broken pickaxe must not be effective on stone");
        helper.assertTrue(brokenPickaxe.getItem().getDestroySpeed(brokenPickaxe, stone) <= 1.0F,
            "Broken pickaxe must not keep its TConstruct mining speed; speed=" + brokenPickaxe.getItem().getDestroySpeed(brokenPickaxe, stone));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void autosmeltModifierProcessesBlockLootThroughSmeltingRecipes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack autosmeltPickaxe = buildIronPickaxeWithModifier(ModifierIds.autosmelt);
        ToolStack tool = ToolStack.from(autosmeltPickaxe);
        helper.assertTrue(tool.getModifierLevel(ModifierIds.autosmelt) == 1,
            "Autosmelt test pickaxe must carry the autosmelt modifier; modifiers=" + tool.getModifierList());
        helper.assertTrue(autosmeltPickaxe.is(TinkerTags.Items.LOOT_CAPABLE_TOOL),
            "Autosmelt pickaxe must be visible to the modifier loot hook; stack=" + autosmeltPickaxe);

        BlockPos orePos = helper.absolutePos(new BlockPos(2, 1, 2));
        List<ItemStack> drops = new java.util.ArrayList<>(List.of(
            new ItemStack(Items.RAW_IRON, 2),
            new ItemStack(Items.DIRT)));
        new AutosmeltModule(1.0F, RecipeType.SMELTING).processLoot(
            tool,
            new ModifierEntry(ModifierIds.autosmelt, 1),
            drops,
            makeLootContext(level, orePos, Blocks.IRON_ORE.defaultBlockState(), autosmeltPickaxe));

        int ironIngots = drops.stream().filter(stack -> stack.is(Items.IRON_INGOT)).mapToInt(ItemStack::getCount).sum();
        int dirt = drops.stream().filter(stack -> stack.is(Items.DIRT)).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(ironIngots == 2,
            "Autosmelt modifier must convert multi-count raw iron loot through vanilla smelting recipes; ingots=" + ironIngots + ", drops=" + drops);
        helper.assertTrue(drops.stream().noneMatch(stack -> stack.is(Items.RAW_IRON)),
            "Autosmelt modifier must replace raw iron drops instead of keeping unsmelted raw iron; drops=" + drops);
        helper.assertTrue(dirt == 1,
            "Autosmelt modifier must leave drops without smelting recipes unchanged; dirt=" + dirt + ", drops=" + drops);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void smeltingModifierCooksToolInventoryOnMeleeHit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack smeltingPickaxe = buildIronPickaxeWithModifier(ModifierIds.smelting);
        ToolStack tool = ToolStack.from(smeltingPickaxe);
        ModifierEntry smeltingEntry = tool.getModifiers().getEntry(ModifierIds.smelting);
        helper.assertTrue(smeltingEntry != ModifierEntry.EMPTY,
            "Test tool must carry the smelting modifier; modifiers=" + tool.getModifierList());

        ResourceLocation inputKey = ModifierIds.smelting.getLocation();
        ResourceLocation outputKey = inputKey.withSuffix("_output");
        SmeltingModule smelting = new SmeltingModule(RecipeType.SMELTING, 10.0F, InventoryModule.builder().slotsPerLevel(1), outputKey, Patterns.RESULT);
        ListTag input = new ListTag();
        input.add(InventoryModule.writeStack(new ItemStack(Items.RAW_IRON, 2), 0, new CompoundTag()));
        tool.getPersistentData().put(inputKey, input);
        helper.assertTrue(smelting.input().getSlots(tool, smeltingEntry) == 1,
            "Smelting input inventory must expose one slot at modifier level 1; slots=" + smelting.input().getSlots(tool, smeltingEntry));
        helper.assertTrue(smelting.output().getSlots(tool, smeltingEntry) == 1,
            "Smelting output inventory must expose one slot at modifier level 1; slots=" + smelting.output().getSlots(tool, smeltingEntry));
        ItemStack initialInput = InventoryModule.readStack(level.registryAccess(), tool.getPersistentData().get(inputKey, InventoryModule.GET_COMPOUND_LIST).getCompound(0));
        helper.assertTrue(initialInput.is(Items.RAW_IRON) && initialInput.getCount() == 2,
            "Smelting input setup must preserve the raw iron stack before cooking; input=" + initialInput + ", raw=" + tool.getPersistentData().get(inputKey, InventoryModule.GET_COMPOUND_LIST));
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(initialInput), level).isPresent(),
            "Vanilla raw iron smelting recipe must be loaded before testing the smelting modifier");

        Player player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, tool.createStack());
        Sheep target = EntityType.SHEEP.create(level);
        helper.assertTrue(target != null, "Smelting melee test must be able to create a sheep target");
        target.moveTo(helper.absolutePos(new BlockPos(2, 1, 2)).getCenter());
        level.addFreshEntity(target);

        ToolAttackContext context = ToolAttackContext.attacker(player)
            .hand(InteractionHand.MAIN_HAND)
            .target(target)
            .build();
        smelting.afterMeleeHit(tool, smeltingEntry, context, 20.0F);

        ListTag outputAfterFirstHit = tool.getPersistentData().get(outputKey, InventoryModule.GET_COMPOUND_LIST);
        ListTag inputAfterFirstHit = tool.getPersistentData().get(inputKey, InventoryModule.GET_COMPOUND_LIST);
        helper.assertTrue(outputAfterFirstHit.size() == 1, "Smelting modifier must create one output slot after a full-cook melee hit; output="
            + outputAfterFirstHit + ", input=" + inputAfterFirstHit);
        ItemStack firstCooked = InventoryModule.readStack(level.registryAccess(), outputAfterFirstHit.getCompound(0));
        helper.assertTrue(firstCooked.is(Items.IRON_INGOT) && firstCooked.getCount() == 1,
            "Smelting modifier must cook raw iron from tool inventory into one iron ingot after one hit; output=" + firstCooked);
        ItemStack remainingRawIron = InventoryModule.readStack(level.registryAccess(), inputAfterFirstHit.getCompound(0));
        helper.assertTrue(remainingRawIron.is(Items.RAW_IRON) && remainingRawIron.getCount() == 1,
            "Smelting modifier must consume exactly one raw iron per completed cook; remaining=" + remainingRawIron + ", input=" + inputAfterFirstHit);

        smelting.afterMeleeHit(tool, smeltingEntry, context, 20.0F);
        ListTag outputAfterSecondHit = tool.getPersistentData().get(outputKey, InventoryModule.GET_COMPOUND_LIST);
        ItemStack secondCooked = InventoryModule.readStack(level.registryAccess(), outputAfterSecondHit.getCompound(0));
        helper.assertTrue(secondCooked.is(Items.IRON_INGOT) && secondCooked.getCount() == 2,
            "Smelting modifier must stack repeated cooked results in its output inventory; output=" + secondCooked + ", raw=" + outputAfterSecondHit);
        helper.assertTrue(tool.getPersistentData().get(inputKey, InventoryModule.GET_COMPOUND_LIST).isEmpty(),
            "Smelting modifier must remove the input slot after the last raw iron cooks; input=" + tool.getPersistentData().get(inputKey, InventoryModule.GET_COMPOUND_LIST));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void harvestModifierRightClicksCropsStacksAndBerryBushes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack kama = buildIronKama();
        ToolStack tool = ToolStack.from(kama);
        helper.assertTrue(tool.getModifierLevel(ModifierIds.harvest) == 1,
            "Built TConstruct kama must carry the harvest modifier from its tool definition; modifiers=" + tool.getModifierList());
        helper.assertTrue(kama.is(TinkerTags.Items.HARVEST_PRIMARY),
            "Built TConstruct kama must be a primary harvest tool; stack=" + kama);

        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, kama);

        BlockPos wheatPos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(wheatPos.below(), Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(wheatPos, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE), Block.UPDATE_ALL);
        InteractionResult wheatResult = useHeldToolOn(player, wheatPos, Direction.UP);
        BlockState harvestedWheat = level.getBlockState(wheatPos);
        helper.assertTrue(wheatResult.consumesAction(), "Harvest modifier must consume right-click on mature wheat; result=" + wheatResult);
        helper.assertTrue(harvestedWheat.is(Blocks.WHEAT) && harvestedWheat.getValue(CropBlock.AGE) == 0,
            "Harvest modifier must replant mature wheat at age 0 after removing one seed from drops; state=" + harvestedWheat);

        BlockPos caneBase = helper.absolutePos(new BlockPos(6, 1, 1));
        BlockPos caneTop = caneBase.above();
        level.setBlock(caneBase, Blocks.SUGAR_CANE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(caneTop, Blocks.SUGAR_CANE.defaultBlockState(), Block.UPDATE_ALL);
        InteractionResult caneResult = useHeldToolOn(player, caneBase, Direction.UP);
        helper.assertTrue(caneResult.consumesAction(), "Harvest modifier must consume right-click on stackable sugar cane; result=" + caneResult);
        helper.assertTrue(level.getBlockState(caneBase).is(Blocks.SUGAR_CANE) && level.getBlockState(caneTop).isAir(),
            "Harvest modifier must preserve the sugar cane base and break the upper stackable block; base="
                + level.getBlockState(caneBase) + ", top=" + level.getBlockState(caneTop));

        BlockPos berryPos = helper.absolutePos(new BlockPos(11, 1, 1));
        level.setBlock(berryPos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(berryPos, Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE), Block.UPDATE_ALL);
        InteractionResult berryResult = useHeldToolOn(player, berryPos, Direction.UP);
        BlockState harvestedBerry = level.getBlockState(berryPos);
        helper.assertTrue(berryResult.consumesAction(), "Harvest modifier must consume right-click on mature sweet berry bush; result=" + berryResult);
        helper.assertTrue(harvestedBerry.is(Blocks.SWEET_BERRY_BUSH) && harvestedBerry.getValue(SweetBerryBushBlock.AGE) == 1,
            "Harvest modifier must use the 1.21 useWithoutItem path for sweet berry bushes and reduce age to 1; state=" + harvestedBerry);

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void toolActionTransformModifiersUseNeoForgeItemAbilities(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);

        BlockPos logPos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(logPos, Blocks.OAK_LOG.defaultBlockState(), Block.UPDATE_ALL);
        ItemStack stripping = buildIronPickaxeWithModifier(ModifierIds.stripping);
        player.setItemInHand(InteractionHand.MAIN_HAND, stripping);
        InteractionResult stripResult = useHeldToolOn(player, logPos, Direction.UP);
        helper.assertTrue(stripResult.consumesAction(), "Stripping modifier must consume right-click on oak log; result=" + stripResult);
        helper.assertTrue(level.getBlockState(logPos).is(Blocks.STRIPPED_OAK_LOG),
            "Stripping modifier must use NeoForge AXE_STRIP item ability to transform oak log; state=" + level.getBlockState(logPos));

        BlockPos dirtPos = helper.absolutePos(new BlockPos(4, 1, 1));
        level.setBlock(dirtPos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(dirtPos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        ItemStack tilling = buildIronPickaxeWithModifier(ModifierIds.tilling);
        player.setItemInHand(InteractionHand.MAIN_HAND, tilling);
        InteractionResult tillResult = useHeldToolOn(player, dirtPos, Direction.UP);
        helper.assertTrue(tillResult.consumesAction(), "Tilling modifier must consume right-click on dirt with air above; result=" + tillResult);
        helper.assertTrue(level.getBlockState(dirtPos).is(Blocks.FARMLAND),
            "Tilling modifier must use NeoForge HOE_TILL item ability to transform dirt; state=" + level.getBlockState(dirtPos));

        BlockPos grassPos = helper.absolutePos(new BlockPos(7, 1, 1));
        level.setBlock(grassPos, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(grassPos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        ItemStack pathing = buildIronPickaxeWithModifier(ModifierIds.pathing);
        player.setItemInHand(InteractionHand.MAIN_HAND, pathing);
        InteractionResult pathResult = useHeldToolOn(player, grassPos, Direction.UP);
        helper.assertTrue(pathResult.consumesAction(), "Pathing modifier must consume right-click on grass block with air above; result=" + pathResult);
        helper.assertTrue(level.getBlockState(grassPos).is(Blocks.DIRT_PATH),
            "Pathing modifier must use NeoForge SHOVEL_FLATTEN item ability to transform grass block; state=" + level.getBlockState(grassPos));

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 180)
    public static void brushingModifierCompletesBrushableBlocksThroughUseTicks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos brushPos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(brushPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(brushPos, Blocks.SUSPICIOUS_SAND.defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockEntity(brushPos) instanceof BrushableBlockEntity,
            "Suspicious sand must create a brushable block entity before brushing regression");

        ItemStack brushing = buildIronPickaxeWithModifier(ModifierIds.brushing);
        ToolStack tool = ToolStack.from(brushing);
        helper.assertTrue(tool.getModifierLevel(ModifierIds.brushing) == 1,
            "Test tool must carry the brushing modifier; modifiers=" + tool.getModifierList());

        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(brushPos.getX() + 0.5, brushPos.getY() + 1.0, brushPos.getZ() + 2.5, 180, 25);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(brushPos));
        player.setItemInHand(InteractionHand.MAIN_HAND, brushing);

        InteractionResult result = useHeldToolOn(player, brushPos, Direction.UP);
        helper.assertTrue(result.consumesAction(), "Brushing modifier must consume right-click on suspicious sand; result=" + result);
        helper.assertTrue(player.isUsingItem(), "Brushing modifier must start vanilla item use after right-clicking a brushable block");
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getUseAnimation() == UseAnim.BRUSH,
            "Active brushing modifier must expose BRUSH use animation");

        for (int i = 0; i < 10; i++) {
            int timeLeft = 196 - i * 10;
            helper.runAfterDelay(5 + i * 10, () -> {
                ItemStack active = player.getItemInHand(InteractionHand.MAIN_HAND);
                active.getItem().onUseTick(level, player, active, timeLeft);
            });
        }
        helper.runAfterDelay(120, () -> {
            BlockState brushed = level.getBlockState(brushPos);
            helper.assertTrue(brushed.is(Blocks.SAND),
                "Brushing modifier must complete suspicious sand through repeated use ticks and turn it into sand; state=" + brushed);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void heldToolInteractionModifiersTransformBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockServerPlayerInLevel();

        BlockPos fireBase = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(fireBase, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(fireBase.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        ItemStack firestarter = buildIronPickaxeWithModifier(ModifierIds.firestarter);
        player.setItemInHand(InteractionHand.MAIN_HAND, firestarter);
        InteractionResult fireResult = useHeldToolOn(player, fireBase, Direction.UP);
        ToolStack fireTool = ToolStack.from(firestarter);
        helper.assertTrue(fireResult.consumesAction(), "Firestarter modifier must consume right-click block interaction; result=" + fireResult
            + ", modifiers=" + fireTool.getModifierList() + ", freeAbilities=" + fireTool.getFreeSlots(SlotType.ABILITY)
            + ", aboveState=" + level.getBlockState(fireBase.above()));
        helper.assertTrue(level.getBlockState(fireBase.above()).is(Blocks.FIRE),
            "Firestarter modifier must place fire above a valid fire base; state=" + level.getBlockState(fireBase.above()));

        BlockPos glowBase = helper.absolutePos(new BlockPos(3, 1, 1));
        level.setBlock(glowBase, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        ItemStack glowing = buildIronPickaxeWithModifier(ModifierIds.glowing);
        player.setItemInHand(InteractionHand.MAIN_HAND, glowing);
        InteractionResult glowResult = useHeldToolOn(player, glowBase, Direction.UP);
        helper.assertTrue(glowResult.consumesAction(), "Glowing modifier must consume right-click block interaction; result=" + glowResult);
        helper.assertTrue(level.getBlockState(glowBase.above()).is(TinkerCommons.glowBlock.get()),
            "Glowing modifier must place the registered glow block above the clicked block; state=" + level.getBlockState(glowBase.above()));

        BlockPos campfirePos = helper.absolutePos(new BlockPos(5, 1, 1));
        level.setBlock(campfirePos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), Block.UPDATE_ALL);
        ItemStack pathing = buildIronPickaxeWithModifier(ModifierIds.pathing);
        player.setItemInHand(InteractionHand.MAIN_HAND, pathing);
        InteractionResult pathingResult = useHeldToolOn(player, campfirePos, Direction.UP);
        helper.assertTrue(pathingResult.consumesAction(), "Pathing modifier must consume right-click on a lit campfire; result=" + pathingResult);
        helper.assertTrue(!level.getBlockState(campfirePos).getValue(CampfireBlock.LIT),
            "Pathing campfire extinguish module must turn a lit campfire off; state=" + level.getBlockState(campfirePos));

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void slurpingModifierDrinksToolTankFluidEffects(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(!player.isCreative(), "Slurping consumption test must run as a non-creative player");

        ItemStack slurping = buildIronPickaxeWithModifier(ModifierIds.slurping);
        ToolStack tool = ToolStack.from(slurping);
        helper.assertTrue(tool.getModifierList().stream().anyMatch(entry -> entry.getId().equals(ModifierIds.slurping)),
            "Test tool must carry the slurping modifier; modifiers=" + tool.getModifierList());
        helper.assertTrue(ToolTankHelper.TANK_HELPER.getCapacity(tool) >= FluidValues.SIP * 2,
            "Slurping modifier must grant enough tank capacity for fluid effects; capacity=" + ToolTankHelper.TANK_HELPER.getCapacity(tool));

        FluidStack storedVenom = new FluidStack(TinkerFluids.venom.get(), FluidValues.SIP * 2);
        ToolTankHelper.TANK_HELPER.setFluid(tool, storedVenom);
        slurping = tool.createStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, slurping);

        var result = slurping.use(level, player, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.getResult().consumesAction(),
            "Slurping modifier must consume right-click with drinkable tool tank fluid; result=" + result.getResult());
        helper.assertTrue(player.isUsingItem(), "Slurping modifier must start vanilla item use");
        ItemStack active = player.getItemInHand(InteractionHand.MAIN_HAND);
        helper.assertTrue(active.getUseAnimation() == UseAnim.DRINK,
            "Slurping modifier must expose DRINK use animation while active; animation=" + active.getUseAnimation());
        helper.assertTrue(active.getUseDuration(player) == 21,
            "Slurping modifier must keep the upstream 21 tick drink duration; duration=" + active.getUseDuration(player));

        ItemStack finished = active.finishUsingItem(level, player);
        player.setItemInHand(InteractionHand.MAIN_HAND, finished);
        finished.getItem().onStopUsing(finished, player, 0);

        helper.assertTrue(player.hasEffect(MobEffects.POISON),
            "Slurping venom must apply the upstream poison fluid effect to the user");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST),
            "Slurping venom must apply the upstream strength fluid effect to the user");
        FluidStack remaining = ToolTankHelper.TANK_HELPER.getFluid(ToolStack.from(player.getItemInHand(InteractionHand.MAIN_HAND)));
        helper.assertTrue(remaining.getFluid() == TinkerFluids.venom.get() && remaining.getAmount() == FluidValues.SIP,
            "Slurping must consume exactly one sip from the tool tank; remaining=" + remaining + ", expected=" + FluidValues.SIP);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void splashingModifierAppliesToolTankFluidToEntities(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(4, 2, 4)).getCenter());
        helper.assertTrue(!player.isCreative(), "Splashing consumption test must run as a non-creative player");

        ItemStack splashing = buildIronPickaxeWithModifier(ModifierIds.splashing);
        ToolStack tool = ToolStack.from(splashing);
        helper.assertTrue(splashing.is(TinkerTags.Items.INTERACTABLE_RIGHT),
            "Splashing test tool must pass the entity interaction event item tag; stack=" + splashing);
        ModifierEntry splashingEntry = tool.getModifiers().getEntry(ModifierIds.splashing);
        helper.assertTrue(splashingEntry != ModifierEntry.EMPTY,
            "Test tool must carry the splashing modifier; modifiers=" + tool.getModifierList());
        helper.assertTrue(ToolTankHelper.TANK_HELPER.getCapacity(tool) >= FluidValues.SIP * 2,
            "Splashing modifier must grant enough tank capacity for fluid effects; capacity=" + ToolTankHelper.TANK_HELPER.getCapacity(tool));

        ToolTankHelper.TANK_HELPER.setFluid(tool, new FluidStack(TinkerFluids.venom.get(), FluidValues.SIP * 2));
        splashing = tool.createStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, splashing);

        Sheep target = EntityType.SHEEP.create(level);
        helper.assertTrue(target != null, "Splashing test must be able to create a sheep target");
        target.moveTo(player.getX() + 1.5, player.getY(), player.getZ(), 0, 0);
        level.addFreshEntity(target);

        InteractionResult result = splashingEntry.getHook(ModifierHooks.ENTITY_INTERACT)
            .beforeEntityUse(ToolStack.from(player.getItemInHand(InteractionHand.MAIN_HAND)), splashingEntry, player, target, InteractionHand.MAIN_HAND, InteractionSource.RIGHT_CLICK);
        helper.assertTrue(result.consumesAction(),
            "Splashing modifier must consume entity right-click when the tool tank fluid has entity effects; result=" + result);
        helper.assertTrue(target.hasEffect(MobEffects.POISON),
            "Splashing venom must apply the upstream poison fluid effect to the target");
        helper.assertTrue(target.hasEffect(MobEffects.DAMAGE_BOOST),
            "Splashing venom must apply the upstream strength fluid effect to the target");

        FluidStack remaining = ToolTankHelper.TANK_HELPER.getFluid(ToolStack.from(player.getItemInHand(InteractionHand.MAIN_HAND)));
        helper.assertTrue(remaining.getFluid() == TinkerFluids.venom.get() && remaining.getAmount() == FluidValues.SIP,
            "Splashing must consume exactly one sip from the tool tank for one target; remaining=" + remaining + ", expected=" + FluidValues.SIP);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void spittingModifierChargesAndShootsToolTankFluidProjectile(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(!player.isCreative(), "Spitting projectile test must run as a non-creative player");

        ItemStack spitting = ToolBuildHandler.buildItemFromMaterials(TinkerTools.earthStaff.get(), MaterialNBT.builder()
            .add(MaterialIds.slimewood)
            .build());
        ToolStack tool = ToolStack.from(spitting);
        tool.getPersistentData().setSlots(SlotType.ABILITY, 3);
        tool.addModifier(ModifierIds.spitting, 1);
        helper.assertTrue(tool.getModifiers().getLevel(ModifierIds.spitting) == 1,
            "Test tool must carry the spitting modifier; modifiers=" + tool.getModifierList());
        assertFloatEquals(helper, 1F, tool.getStats().get(ToolStats.PROJECTILE_DAMAGE), 0.0001F,
            "Earth staff must keep upstream projectile damage used by spitting fluid cost");
        helper.assertTrue(ToolTankHelper.TANK_HELPER.getCapacity(tool) >= 500,
            "Spitting modifier must grant enough tank capacity for projectile fluid; capacity=" + ToolTankHelper.TANK_HELPER.getCapacity(tool));

        FluidEffects lavaEffects = FluidEffectManager.INSTANCE.find(Fluids.LAVA);
        helper.assertTrue(lavaEffects.hasEffects(), "Lava fluid effects must be loaded for spitting shots");
        int expectedShotAmount = lavaEffects.getAmount(Fluids.LAVA);
        helper.assertTrue(expectedShotAmount == 50,
            "Lava fluid effect amount must match upstream spitting cost; amount=" + expectedShotAmount);

        int initialAmount = 500;
        ToolTankHelper.TANK_HELPER.setFluid(tool, new FluidStack(Fluids.LAVA, initialAmount));
        spitting = tool.createStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, spitting);

        var result = spitting.use(level, player, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.getResult().consumesAction(),
            "Spitting modifier must consume right-click with projectile-capable tool tank fluid; result=" + result.getResult());
        helper.assertTrue(player.isUsingItem(), "Spitting modifier must start vanilla charged item use");
        ItemStack active = player.getItemInHand(InteractionHand.MAIN_HAND);
        helper.assertTrue(active.getUseAnimation() == UseAnim.BOW,
            "Spitting modifier must expose BOW use animation while charging; animation=" + active.getUseAnimation());
        helper.assertTrue(active.getUseDuration(player) == 72000,
            "Spitting modifier must keep the upstream charged use duration; duration=" + active.getUseDuration(player));

        int timeLeft = active.getUseDuration(player) - 80;
        active.releaseUsing(level, player, timeLeft);
        active.getItem().onStopUsing(active, player, timeLeft);

        List<FluidEffectProjectile> projectiles = level.getEntitiesOfClass(FluidEffectProjectile.class, new AABB(player.blockPosition()).inflate(8));
        helper.assertTrue(projectiles.size() == 1, "Spitting release must spawn exactly one fluid effect projectile; count=" + projectiles.size());
        FluidEffectProjectile projectile = projectiles.getFirst();
        helper.assertTrue(projectile.getType() == TinkerModifiers.fluidSpitEntity.get(),
            "Spitting projectile must use the registered tconstruct fluid spit entity type; current=" + projectile.getType());
        helper.assertTrue(projectile.getOwner() == player,
            "Spitting projectile must keep the firing player as owner; owner=" + projectile.getOwner());
        helper.assertTrue(projectile.getFluid().getFluid() == Fluids.LAVA && projectile.getFluid().getAmount() == expectedShotAmount,
            "Spitting projectile must carry exactly one lava fluid effect amount; fluid=" + projectile.getFluid() + ", expected=" + expectedShotAmount);

        FluidStack remaining = ToolTankHelper.TANK_HELPER.getFluid(ToolStack.from(player.getItemInHand(InteractionHand.MAIN_HAND)));
        helper.assertTrue(remaining.getFluid() == Fluids.LAVA && remaining.getAmount() == initialAmount - expectedShotAmount,
            "Spitting must consume exactly the projectile's fluid amount from the tool tank; remaining=" + remaining
                + ", expected=" + (initialAmount - expectedShotAmount));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void bucketingModifierPlacesAndPicksUpWorldFluids(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(!player.isCreative(), "Bucketing world-fluid test must run as a non-creative player");

        ItemStack bucketing = buildIronPickaxeWithModifier(ModifierIds.bucketing);
        ToolStack tool = ToolStack.from(bucketing);
        helper.assertTrue(tool.getModifiers().getLevel(ModifierIds.bucketing) == 1,
            "Test tool must carry the bucketing modifier; modifiers=" + tool.getModifierList());
        helper.assertTrue(ToolTankHelper.TANK_HELPER.getCapacity(tool) == FluidType.BUCKET_VOLUME,
            "Bucketing modifier must grant exactly one bucket of tool tank capacity; capacity=" + ToolTankHelper.TANK_HELPER.getCapacity(tool));

        BlockPos support = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos fluidPos = support.above();
        level.setBlock(support, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(fluidPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        ToolTankHelper.TANK_HELPER.setFluid(tool, new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME));
        bucketing = tool.createStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, bucketing);
        player.setShiftKeyDown(true);

        InteractionResult placeResult = useHeldToolOn(player, support, Direction.UP);
        helper.assertTrue(placeResult.consumesAction(),
            "Sneaking bucketing tool must consume block right-click when placing a bucket of stored lava; result=" + placeResult);
        helper.assertTrue(level.getFluidState(fluidPos).getType() == Fluids.LAVA,
            "Bucketing tool must place stored lava into the clicked neighbor block; fluid=" + level.getFluidState(fluidPos));
        FluidStack afterPlace = ToolTankHelper.TANK_HELPER.getFluid(ToolStack.from(player.getItemInHand(InteractionHand.MAIN_HAND)));
        helper.assertTrue(afterPlace.isEmpty(), "Placing lava must drain the tool tank; remaining=" + afterPlace);

        level.setBlock(fluidPos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        player.setShiftKeyDown(false);
        player.moveTo(fluidPos.getX() + 0.5, fluidPos.getY() + 0.5, fluidPos.getZ() + 3.0, 180, 0);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(fluidPos));

        ItemStack active = player.getItemInHand(InteractionHand.MAIN_HAND);
        var pickupResult = active.use(level, player, InteractionHand.MAIN_HAND);
        helper.assertTrue(pickupResult.getResult().consumesAction(),
            "Non-sneaking bucketing tool must consume right-click when picking up a source fluid; result=" + pickupResult.getResult());
        helper.assertTrue(level.getFluidState(fluidPos).isEmpty(),
            "Bucketing pickup must remove the source water block from the world; fluid=" + level.getFluidState(fluidPos));
        FluidStack afterPickup = ToolTankHelper.TANK_HELPER.getFluid(ToolStack.from(player.getItemInHand(InteractionHand.MAIN_HAND)));
        helper.assertTrue(afterPickup.getFluid() == Fluids.WATER && afterPickup.getAmount() == FluidType.BUCKET_VOLUME,
            "Bucketing pickup must store exactly one bucket of water in the tool tank; fluid=" + afterPickup);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void slimeballModifierConsumesAmmoAndShootsCustomFireball(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(!player.isCreative(), "Slimeball fireball test must run as a non-creative player");

        ItemStack slimeballTool = buildIronPickaxeWithModifier(ModifierIds.slimeball);
        ToolStack tool = ToolStack.from(slimeballTool);
        helper.assertTrue(tool.getModifiers().getLevel(ModifierIds.slimeball) == 1,
            "Test tool must carry the slimeball modifier; modifiers=" + tool.getModifierList());
        helper.assertTrue(tool.getStats().get(ToolStats.PROJECTILE_DAMAGE) > 0 && tool.getStats().get(ToolStats.VELOCITY) > 0,
            "Slimeball modifier test tool must expose positive projectile stats; stats=" + tool.getStats());

        BlockPos start = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(start.relative(Direction.SOUTH, 4)));
        player.setItemInHand(InteractionHand.MAIN_HAND, slimeballTool);
        player.getInventory().add(new ItemStack(Items.SLIME_BALL, 3));

        var result = slimeballTool.use(level, player, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.getResult().consumesAction(),
            "Slimeball modifier must consume right-click when valid slimeball ammo is available; result=" + result.getResult());
        helper.assertTrue(countInventoryItems(player.getInventory(), Items.SLIME_BALL) == 2,
            "Slimeball modifier must consume exactly one slimeball ammo in survival; count=" + countInventoryItems(player.getInventory(), Items.SLIME_BALL));
        helper.assertTrue(player.getCooldowns().isOnCooldown(slimeballTool.getItem()),
            "Slimeball modifier must apply draw-speed based item cooldown after firing");

        List<CustomFireball> projectiles = level.getEntitiesOfClass(CustomFireball.class, new AABB(player.blockPosition()).inflate(8));
        helper.assertTrue(projectiles.size() == 1,
            "Slimeball modifier must spawn exactly one registered custom fireball projectile; count=" + projectiles.size());
        CustomFireball projectile = projectiles.getFirst();
        helper.assertTrue(projectile.getType() == TinkerModifiers.fireball.get(),
            "Slimeball projectile must use the registered tconstruct fireball entity type; current=" + projectile.getType());
        helper.assertTrue(projectile.getOwner() == player,
            "Slimeball projectile must keep the firing player as owner; owner=" + projectile.getOwner());
        helper.assertTrue(projectile.getItem().is(Items.SLIME_BALL),
            "Slimeball projectile must carry the consumed slimeball ammo stack; item=" + projectile.getItem());
        helper.assertTrue(projectile.getPower() == tool.getStats().get(ToolStats.PROJECTILE_DAMAGE),
            "Slimeball projectile power must inherit projectile damage stat; power=" + projectile.getPower()
                + ", expected=" + tool.getStats().get(ToolStats.PROJECTILE_DAMAGE));
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void throwingModifierChargesAndThrowsHeldTool(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(!player.isCreative(), "Throwing projectile test must run as a non-creative player");

        ItemStack throwing = buildIronPickaxeWithModifier(ModifierIds.throwing);
        ToolStack tool = ToolStack.from(throwing);
        helper.assertTrue(throwing.is(TinkerTags.Items.MELEE_WEAPON),
            "Throwing test tool must be eligible as a melee weapon; stack=" + throwing);
        helper.assertTrue(tool.getModifiers().getLevel(ModifierIds.throwing) == 1,
            "Test tool must carry the throwing modifier; modifiers=" + tool.getModifierList());

        BlockPos start = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(start.relative(Direction.SOUTH, 4)));
        player.setItemInHand(InteractionHand.MAIN_HAND, throwing);

        var result = throwing.use(level, player, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.getResult().consumesAction(),
            "Throwing modifier must consume right-click to start charged tool throwing; result=" + result.getResult());
        helper.assertTrue(player.isUsingItem(), "Throwing modifier must start vanilla charged item use");
        ItemStack active = player.getItemInHand(InteractionHand.MAIN_HAND);
        helper.assertTrue(active.getUseAnimation() == UseAnim.SPEAR,
            "Throwing modifier must expose SPEAR use animation while charging; animation=" + active.getUseAnimation());
        helper.assertTrue(active.getUseDuration(player) == 72000,
            "Throwing modifier must keep the upstream charged use duration; duration=" + active.getUseDuration(player));

        int timeLeft = active.getUseDuration(player) - 40;
        active.releaseUsing(level, player, timeLeft);
        active.getItem().onStopUsing(active, player, timeLeft);

        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(),
            "Throwing release must remove the thrown tool from the survival player's hand; hand=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        List<ThrownTool> projectiles = level.getEntitiesOfClass(ThrownTool.class, new AABB(player.blockPosition()).inflate(8));
        helper.assertTrue(projectiles.size() == 1,
            "Throwing release must spawn exactly one thrown tool projectile; count=" + projectiles.size());
        ThrownTool projectile = projectiles.getFirst();
        helper.assertTrue(projectile.getType() == TinkerTools.thrownTool.get(),
            "Throwing projectile must use the registered tconstruct thrown_tool entity type; current=" + projectile.getType());
        helper.assertTrue(projectile.getOwner() == player,
            "Throwing projectile must keep the firing player as owner; owner=" + projectile.getOwner());
        ItemStack pickup = projectile.getPickupItemStackOrigin();
        helper.assertTrue(pickup.is(TinkerTools.pickaxe.get()),
            "Throwing projectile must carry the thrown pickaxe stack; pickup=" + pickup);
        helper.assertTrue(ToolStack.from(pickup).getModifiers().getLevel(ModifierIds.throwing) == 1,
            "Throwing projectile pickup stack must preserve tool modifiers; modifiers=" + ToolStack.from(pickup).getModifierList());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void returningModifierReturnsThrownToolToOriginalSlot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(!player.isCreative(), "Returning thrown-tool test must run as a non-creative player");

        ToolStack tool = ToolStack.from(buildIronPickaxe());
        tool.getPersistentData().setSlots(SlotType.ABILITY, 3);
        tool.addModifier(ModifierIds.throwing, 1);
        tool.addModifier(ModifierIds.returning, 1);
        ItemStack returning = tool.createStack();
        helper.assertTrue(ToolStack.from(returning).getModifiers().getLevel(ModifierIds.throwing) == 1
                && ToolStack.from(returning).getModifiers().getLevel(ModifierIds.returning) == 1,
            "Returning test tool must carry both throwing and returning modifiers; modifiers=" + ToolStack.from(returning).getModifierList());

        BlockPos start = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(start.relative(Direction.SOUTH, 4)));
        player.setItemInHand(InteractionHand.MAIN_HAND, returning);

        var result = returning.use(level, player, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.getResult().consumesAction(),
            "Returning throwing tool must consume right-click to start charged use; result=" + result.getResult());
        ItemStack active = player.getItemInHand(InteractionHand.MAIN_HAND);
        int timeLeft = active.getUseDuration(player) - 40;
        active.releaseUsing(level, player, timeLeft);
        active.getItem().onStopUsing(active, player, timeLeft);

        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(),
            "Throwing a returning tool must remove it from the original hand before return; hand=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        List<ThrownTool> projectiles = level.getEntitiesOfClass(ThrownTool.class, new AABB(player.blockPosition()).inflate(8));
        helper.assertTrue(projectiles.size() == 1,
            "Returning throw must spawn exactly one thrown tool projectile; count=" + projectiles.size());
        ThrownTool projectile = projectiles.getFirst();
        ItemStack pickup = projectile.getPickupItemStackOrigin();
        helper.assertTrue(ToolStack.from(pickup).getModifiers().getLevel(ModifierIds.returning) == 1,
            "Thrown pickup stack must preserve returning modifier before return; modifiers=" + ToolStack.from(pickup).getModifierList());

        setThrownToolDealtDamage(projectile);
        projectile.tick();
        helper.assertTrue(projectile.isNoPhysics(),
            "Returning modifier must switch the thrown tool into no-physics return mode after impact");
        helper.assertTrue(projectile.getDeltaMovement().lengthSqr() > 0,
            "Returning thrown tool must gain return motion toward its owner; motion=" + projectile.getDeltaMovement());

        projectile.setPos(player.getX(), player.getEyeY(), player.getZ());
        projectile.playerTouch(player);
        helper.assertTrue(projectile.isRemoved(),
            "Returning thrown tool must be removed after the owner picks it up");
        ItemStack returned = player.getItemInHand(InteractionHand.MAIN_HAND);
        helper.assertTrue(returned.is(TinkerTools.pickaxe.get()),
            "Returning thrown tool must restore the pickaxe to the original selected slot; returned=" + returned);
        helper.assertTrue(ToolStack.from(returned).getModifiers().getLevel(ModifierIds.returning) == 1,
            "Returned tool stack must preserve the returning modifier; modifiers=" + ToolStack.from(returned).getModifierList());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void channelingModifierStrikesLightningFromThrownToolHit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(level.getLevelData() instanceof ServerLevelData,
            "Channeling test must run on mutable server level data; data=" + level.getLevelData().getClass().getName());
        ServerLevelData levelData = (ServerLevelData)level.getLevelData();
        levelData.setClearWeatherTime(0);
        levelData.setRainTime(6000);
        levelData.setThunderTime(6000);
        levelData.setRaining(true);
        levelData.setThundering(true);
        level.setRainLevel(1.0f);
        level.setThunderLevel(1.0f);
        helper.assertTrue(level.isThundering(), "Channeling test must run during thunder for guaranteed upstream chance");

        var player = helper.makeMockServerPlayerInLevel();
        ToolStack tool = ToolStack.from(buildIronPickaxe());
        tool.getPersistentData().setSlots(SlotType.ABILITY, 3);
        tool.addModifier(ModifierIds.throwing, 1);
        tool.addModifier(ModifierIds.channeling, 1);
        ItemStack channeling = tool.createStack();
        helper.assertTrue(ToolStack.from(channeling).getModifiers().getLevel(ModifierIds.channeling) == 1,
            "Channeling thrown-tool test stack must carry channeling; modifiers=" + ToolStack.from(channeling).getModifierList());

        BlockPos start = helper.absolutePos(new BlockPos(2, 20, 2));
        BlockPos targetPos = start.relative(Direction.SOUTH, 4);
        helper.assertTrue(level.canSeeSky(targetPos),
            "Channeling lightning target must see sky; target=" + targetPos);
        player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(targetPos));
        player.setItemInHand(InteractionHand.MAIN_HAND, channeling);

        var result = channeling.use(level, player, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.getResult().consumesAction(),
            "Channeling thrown tool must consume right-click to start throwing; result=" + result.getResult());
        ItemStack active = player.getItemInHand(InteractionHand.MAIN_HAND);
        int timeLeft = active.getUseDuration(player) - 40;
        active.releaseUsing(level, player, timeLeft);
        active.getItem().onStopUsing(active, player, timeLeft);

        List<ThrownTool> projectiles = level.getEntitiesOfClass(ThrownTool.class, new AABB(player.blockPosition()).inflate(12));
        helper.assertTrue(projectiles.size() == 1,
            "Channeling throw must spawn exactly one thrown tool projectile; count=" + projectiles.size());
        ThrownTool projectile = projectiles.getFirst();
        ToolStack thrownTool = ToolStack.from(projectile.getPickupItemStackOrigin());
        ModifierEntry channelingEntry = thrownTool.getModifiers().getEntry(ModifierIds.channeling);
        helper.assertTrue(channelingEntry != ModifierEntry.EMPTY,
            "Thrown projectile pickup stack must preserve channeling modifier; modifiers=" + thrownTool.getModifierList());

        Sheep target = EntityType.SHEEP.create(level);
        helper.assertTrue(target != null, "Channeling test must be able to create a hit target");
        target.moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 0, 0);
        level.addFreshEntity(target);
        ToolAttackContext context = ToolAttackContext.attacker(player)
            .projectile(projectile)
            .target(target)
            .cooldown(1.0f)
            .build();

        int lightningBefore = level.getEntitiesOfClass(LightningBolt.class, new AABB(targetPos).inflate(4)).size();
        channelingEntry.getHook(ModifierHooks.MELEE_HIT).afterMeleeHit(thrownTool, channelingEntry, context, 1.0f);
        List<LightningBolt> lightning = level.getEntitiesOfClass(LightningBolt.class, new AABB(targetPos).inflate(4));
        helper.assertTrue(lightning.size() == lightningBefore + 1,
            "Channeling thrown projectile hit must spawn exactly one lightning bolt at the target; before=" + lightningBefore + ", after=" + lightning.size());
        helper.assertTrue(lightning.getLast().getCause() == player,
            "Channeling lightning bolt must record the throwing server player as cause; cause=" + lightning.getLast().getCause());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void castingTableCoolsMoltenIronIntoIngot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos tablePos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(tablePos, TinkerSmeltery.searedTable.get().defaultBlockState(), Block.UPDATE_ALL);

        CastingBlockEntity table = (CastingBlockEntity) level.getBlockEntity(tablePos);
        helper.assertTrue(table != null, "Seared casting table must create a casting block entity");
        helper.assertTrue(level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("tconstruct", "smeltery/casting/metal/iron/ingot_gold_cast")).isPresent(),
            "Iron ingot casting recipe must load before table casting can work");

        table.setItem(CastingBlockEntity.INPUT, new ItemStack(TinkerSmeltery.ingotCast.get()));
        int filled = table.getTank().fill(new FluidStack(TinkerFluids.moltenIron.get(), 90), FluidAction.EXECUTE);
        helper.assertTrue(filled == 90, "Casting table must accept exactly one ingot of molten iron; filled=" + filled
            + ", capacity=" + table.getTank().getTankCapacity(0) + ", fluid=" + table.getTank().getFluidInTank(0));

        helper.runAfterDelay(70, () -> {
            ItemStack cast = table.getItem(CastingBlockEntity.INPUT);
            ItemStack output = table.getItem(CastingBlockEntity.OUTPUT);
            helper.assertTrue(cast.is(TinkerSmeltery.ingotCast.get()), "Casting table must preserve the reusable ingot cast; input=" + cast);
            helper.assertTrue(output.is(Items.IRON_INGOT), "Casting table must output an iron ingot; output=" + output);
            helper.assertTrue(table.getTank().getFluidInTank(0).isEmpty(), "Casting table tank must be empty after cooling; fluid=" + table.getTank().getFluidInTank(0));
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void castingTableAcceptsManualCastAndCoolsWithoutCrash(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos tablePos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(tablePos, TinkerSmeltery.searedTable.get().defaultBlockState(), Block.UPDATE_ALL);

        CastingBlockEntity table = (CastingBlockEntity) level.getBlockEntity(tablePos);
        helper.assertTrue(table != null, "Manual casting table test must create a casting block entity");
        helper.assertTrue(level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("tconstruct", "smeltery/casting/metal/iron/ingot_gold_cast")).isPresent(),
            "Manual casting table test requires the iron ingot gold-cast recipe to be loaded");
        helper.assertTrue(TinkerItemDisplays.CASTING_TABLE.isModded()
                && "tconstruct:casting_table".equals(TinkerItemDisplays.CASTING_TABLE.getSerializedName())
                && TinkerItemDisplays.CASTING_TABLE.fallback() == ItemDisplayContext.FIXED,
            "Casting table render transform must be the TConstruct enum extension, not vanilla fixed; context="
                + TinkerItemDisplays.CASTING_TABLE + ", serialized=" + TinkerItemDisplays.CASTING_TABLE.getSerializedName()
                + ", modded=" + TinkerItemDisplays.CASTING_TABLE.isModded() + ", fallback=" + TinkerItemDisplays.CASTING_TABLE.fallback());

        Player player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TinkerSmeltery.ingotCast.get()));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(tablePos), Direction.UP, tablePos, false);
        var interaction = level.getBlockState(tablePos).useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(interaction.consumesAction(),
            "Casting table block-state right-click with a held cast must consume the interaction; result=" + interaction);
        helper.assertTrue(table.getItem(CastingBlockEntity.INPUT).is(TinkerSmeltery.ingotCast.get()),
            "Casting table must accept a reusable ingot cast through manual right-click; input=" + table.getItem(CastingBlockEntity.INPUT)
                + ", hand=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(),
            "Manual cast insertion must consume exactly the held single cast stack; hand=" + player.getItemInHand(InteractionHand.MAIN_HAND));

        int filled = table.getTank().fill(new FluidStack(TinkerFluids.moltenIron.get(), FluidValues.INGOT), FluidAction.EXECUTE);
        helper.assertTrue(filled == FluidValues.INGOT,
            "Casting table with a manually inserted cast must accept one ingot of molten iron; filled=" + filled
                + ", tank=" + table.getTank().getFluidInTank(0));

        helper.runAfterDelay(70, () -> {
            helper.assertTrue(table.getItem(CastingBlockEntity.INPUT).is(TinkerSmeltery.ingotCast.get()),
                "Casting table must preserve manually inserted reusable cast after cooling; input=" + table.getItem(CastingBlockEntity.INPUT));
            helper.assertTrue(table.getItem(CastingBlockEntity.OUTPUT).is(Items.IRON_INGOT),
                "Casting table must output an iron ingot after manual cast insertion; output=" + table.getItem(CastingBlockEntity.OUTPUT));
            helper.assertTrue(table.getTank().getFluidInTank(0).isEmpty(),
                "Casting table tank must be empty after cooling with manual cast; fluid=" + table.getTank().getFluidInTank(0));
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 120)
    public static void faucetPartiallyDrainsMoltenIronIntoCastingTable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos controllerPos = helper.absolutePos(new BlockPos(4, 2, 2));
        BlockPos drainPos = controllerPos.offset(-1, 0, 1);
        BlockPos faucetPos = drainPos.relative(Direction.WEST);
        BlockPos tablePos = faucetPos.below();

        buildMinimalHeatingStructure(level, controllerPos, true);
        level.setBlock(drainPos, TinkerSmeltery.searedDrain.get().defaultBlockState()
            .setValue(OrientableSmelteryBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        level.setBlock(faucetPos, TinkerSmeltery.searedFaucet.get().defaultBlockState()
            .setValue(FaucetBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        level.setBlock(tablePos, TinkerSmeltery.searedTable.get().defaultBlockState(), Block.UPDATE_ALL);

        SmelteryBlockEntity smeltery = (SmelteryBlockEntity) level.getBlockEntity(controllerPos);
        FaucetBlockEntity faucet = (FaucetBlockEntity) level.getBlockEntity(faucetPos);
        CastingBlockEntity table = (CastingBlockEntity) level.getBlockEntity(tablePos);
        helper.assertTrue(smeltery != null, "Partial faucet test must create a smeltery controller block entity");
        helper.assertTrue(faucet != null, "Partial faucet test must create a faucet block entity");
        helper.assertTrue(table != null, "Partial faucet test must create a casting table block entity");

        table.setItem(CastingBlockEntity.INPUT, new ItemStack(TinkerSmeltery.ingotCast.get()));
        smeltery.updateStructure();

        helper.runAfterDelay(2, () -> {
            assertRealHeatingStructure(helper, smeltery, controllerPos, "Partial smeltery faucet source");
            int filled = smeltery.getTank().fill(new FluidStack(TinkerFluids.moltenIron.get(), FluidValues.INGOT / 2), FluidAction.EXECUTE);
            helper.assertTrue(filled == FluidValues.INGOT / 2,
                "Formed smeltery tank must accept the partial molten iron amount before faucet transfer; filled=" + filled);
            faucet.activate();
        });

        helper.runAfterDelay(55, () -> {
            FluidStack smelteryFluid = smeltery.getTank().getFluidInTank(0);
            FluidStack tableFluid = table.getTank().getFluidInTank(0);
            String state = "smelteryFluid=" + smelteryFluid
                + ", tableFluid=" + tableFluid
                + ", capacity=" + table.getTank().getTankCapacity(0)
                + ", coolingTime=" + table.getCoolingTime()
                + ", cast=" + table.getItem(CastingBlockEntity.INPUT)
                + ", output=" + table.getItem(CastingBlockEntity.OUTPUT)
                + ", recipeOutput=" + table.getRecipeOutput();
            helper.assertTrue(smelteryFluid.isEmpty(), "Faucet must drain all available partial source fluid; " + state);
            helper.assertTrue(tableFluid.is(TinkerFluids.moltenIron.get()) && tableFluid.getAmount() == FluidValues.INGOT / 2,
                "Casting table must keep partial molten iron instead of rejecting the transfer; " + state);
            helper.assertTrue(table.getTank().getTankCapacity(0) == FluidValues.INGOT,
                "Casting table partial tank capacity must remain the matched ingot recipe amount; " + state);
            helper.assertTrue(table.getCoolingTime() < 0,
                "Casting table must not start cooling until the matched recipe is fully filled; " + state);
            helper.assertTrue(table.getItem(CastingBlockEntity.INPUT).is(TinkerSmeltery.ingotCast.get()),
                "Casting table must keep the reusable cast locked while partial fluid is present; " + state);
            helper.assertTrue(table.getItem(CastingBlockEntity.OUTPUT).isEmpty(),
                "Casting table must not create output from a partial fill; " + state);
            helper.assertTrue(table.getRecipeOutput().is(Items.IRON_INGOT),
                "Casting table must keep the matched ingot recipe for later completion; " + state);

            Player player = helper.makeMockServerPlayerInLevel();
            level.getBlockState(tablePos).useWithoutItem(level, player, new BlockHitResult(Vec3.atCenterOf(tablePos), Direction.UP, tablePos, false));
            helper.assertTrue(table.getItem(CastingBlockEntity.INPUT).is(TinkerSmeltery.ingotCast.get()),
                "Right-clicking a partially filled casting table must not pick up or replace the cast; " + state
                    + ", afterInput=" + table.getItem(CastingBlockEntity.INPUT));
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 240)
    public static void castingBasinCoolsMoltenIronIntoBlock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos basinPos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(basinPos, TinkerSmeltery.searedBasin.get().defaultBlockState(), Block.UPDATE_ALL);

        CastingBlockEntity basin = (CastingBlockEntity) level.getBlockEntity(basinPos);
        helper.assertTrue(basin != null, "Seared casting basin must create a casting block entity");
        helper.assertTrue(level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("tconstruct", "smeltery/casting/metal/iron/block")).isPresent(),
            "Iron block casting recipe must load before basin casting can work");

        int filled = basin.getTank().fill(new FluidStack(TinkerFluids.moltenIron.get(), 810), FluidAction.EXECUTE);
        helper.assertTrue(filled == 810, "Casting basin must accept exactly one block of molten iron; filled=" + filled
            + ", capacity=" + basin.getTank().getTankCapacity(0) + ", fluid=" + basin.getTank().getFluidInTank(0));

        helper.runAfterDelay(190, () -> {
            ItemStack output = basin.getItem(CastingBlockEntity.OUTPUT);
            helper.assertTrue(output.is(Items.IRON_BLOCK), "Casting basin must output an iron block; output=" + output);
            helper.assertTrue(basin.getTank().getFluidInTank(0).isEmpty(), "Casting basin tank must be empty after cooling; fluid=" + basin.getTank().getFluidInTank(0));
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 180)
    public static void faucetDrainsSmelteryIntoCastingTable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos controllerPos = helper.absolutePos(new BlockPos(4, 2, 2));
        BlockPos drainPos = controllerPos.offset(-1, 0, 1);
        BlockPos faucetPos = drainPos.relative(Direction.WEST);
        BlockPos tablePos = faucetPos.below();

        buildMinimalHeatingStructure(level, controllerPos, true);
        level.setBlock(drainPos, TinkerSmeltery.searedDrain.get().defaultBlockState()
            .setValue(OrientableSmelteryBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        level.setBlock(faucetPos, TinkerSmeltery.searedFaucet.get().defaultBlockState()
            .setValue(FaucetBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        level.setBlock(tablePos, TinkerSmeltery.searedTable.get().defaultBlockState(), Block.UPDATE_ALL);

        SmelteryBlockEntity smeltery = (SmelteryBlockEntity) level.getBlockEntity(controllerPos);
        DrainBlockEntity drain = (DrainBlockEntity) level.getBlockEntity(drainPos);
        FaucetBlockEntity faucet = (FaucetBlockEntity) level.getBlockEntity(faucetPos);
        CastingBlockEntity table = (CastingBlockEntity) level.getBlockEntity(tablePos);
        helper.assertTrue(smeltery != null, "Smeltery controller must create a block entity for faucet transfer");
        helper.assertTrue(drain != null, "Seared drain must create a block entity for faucet transfer");
        helper.assertTrue(faucet != null, "Seared faucet must create a block entity for faucet transfer");
        helper.assertTrue(table != null, "Seared casting table must create a block entity for faucet transfer");

        table.setItem(CastingBlockEntity.INPUT, new ItemStack(TinkerSmeltery.ingotCast.get()));
        smeltery.updateStructure();

        helper.runAfterDelay(2, () -> {
            assertRealHeatingStructure(helper, smeltery, controllerPos, "Smeltery faucet source");

            IFluidHandler drainCapability = level.getCapability(Capabilities.FluidHandler.BLOCK, drainPos, level.getBlockState(drainPos), drain, Direction.WEST);
            IFluidHandler tableCapability = level.getCapability(Capabilities.FluidHandler.BLOCK, tablePos, level.getBlockState(tablePos), table, Direction.UP);
            helper.assertTrue(drainCapability != null, "Formed drain must expose smeltery fluid capability on faucet side");
            helper.assertTrue(tableCapability != null, "Casting table must expose fluid capability to faucet output");

            int filled = smeltery.getTank().fill(new FluidStack(TinkerFluids.moltenIron.get(), 90), FluidAction.EXECUTE);
            helper.assertTrue(filled == 90, "Formed smeltery tank must accept one ingot of molten iron before faucet transfer; filled=" + filled
                + ", capacity=" + smeltery.getTank().getCapacity());
            faucet.activate();
        });

        helper.runAfterDelay(90, () -> {
            ItemStack cast = table.getItem(CastingBlockEntity.INPUT);
            ItemStack output = table.getItem(CastingBlockEntity.OUTPUT);
            FluidStack smelteryFluid = smeltery.getTank().getFluidInTank(0);
            FluidStack tableFluid = table.getTank().getFluidInTank(0);
            String state = "smelteryFluid=" + smelteryFluid
                + ", tableFluid=" + tableFluid
                + ", cast=" + cast
                + ", output=" + output
                + ", faucetPouring=" + faucet.isPouring();
            helper.assertTrue(cast.is(TinkerSmeltery.ingotCast.get()), "Faucet casting must preserve reusable ingot cast; " + state);
            helper.assertTrue(output.is(Items.IRON_INGOT), "Faucet must drain molten iron into table and produce an iron ingot; " + state);
            helper.assertTrue(smelteryFluid.isEmpty(), "Faucet must drain the source smeltery tank; " + state);
            helper.assertTrue(tableFluid.isEmpty(), "Casting table fluid must be consumed after cooling; " + state);
            helper.succeed();
        });
    }

    private static void assertHeatingControllerMenu(GameTestHelper helper, HeatingStructureBlockEntity controller, BlockPos pos, String name) {
        helper.assertTrue(controller != null, name + " controller must create a block entity");
        controller.setStructureSize(pos.offset(-1, -1, -1), pos.offset(1, 1, 1), List.of(pos.below()));
        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        var menu = controller.createMenu(9, player.getInventory(), player);
        helper.assertTrue(menu instanceof HeatingStructureContainerMenu,
            name + " controller must create a heating structure menu; current=" + (menu == null ? "null" : menu.getClass().getName()));
        helper.assertTrue(menu.getType() == TinkerSmeltery.smelteryContainer.get(),
            name + " controller menu must use the shared smeltery menu type; current=" + menu.getType());
    }

    private static void assertRealHeatingStructure(GameTestHelper helper, HeatingStructureBlockEntity controller, BlockPos pos, String name) {
        helper.assertTrue(controller.getStructure() != null,
            name + " must form a real multiblock; result=" + controller.getStructureResult().getMessage());
        helper.assertTrue(controller.getBlockState().getValue(ControllerBlock.IN_STRUCTURE),
            name + " controller must be marked in-structure after formation");
        helper.assertTrue(controller.getStructure().hasTanks(), name + " real structure must detect its fuel tank");
        helper.assertTrue(controller.getTank() != null, name + " real structure must expose a smeltery tank");
        helper.assertTrue(controller.getMeltingInventory().getSlots() > 0,
            name + " real structure must resize melting inventory from formed inside volume");

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        var menu = controller.createMenu(10, player.getInventory(), player);
        helper.assertTrue(menu instanceof HeatingStructureContainerMenu,
            name + " real structure must create a heating structure menu; current=" + (menu == null ? "null" : menu.getClass().getName()));
        helper.assertTrue(menu.getType() == TinkerSmeltery.smelteryContainer.get(),
            name + " real structure menu must use the shared smeltery menu type; current=" + menu.getType());
    }

    private static void assertTableMenuAndCapability(GameTestHelper helper, ServerLevel level, BlockPos relativePos, Block block, Class<? extends BlockEntity> blockEntityClass,
                                                     Class<? extends AbstractContainerMenu> menuClass, MenuType<?> menuType, int minItemSlots, String name) {
        BlockPos pos = helper.absolutePos(relativePos);
        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        helper.assertTrue(blockEntityClass.isInstance(blockEntity),
            name + " must create " + blockEntityClass.getSimpleName() + "; current=" + (blockEntity == null ? "null" : blockEntity.getClass().getName()));

        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, level.getBlockState(pos), blockEntity, null);
        helper.assertTrue(itemHandler != null, name + " must expose item handler capability");
        helper.assertTrue(itemHandler.getSlots() >= minItemSlots,
            name + " item handler must expose expected slots; current=" + itemHandler.getSlots() + ", expectedAtLeast=" + minItemSlots);

        var player = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        AbstractContainerMenu menu = ((net.minecraft.world.MenuProvider)blockEntity).createMenu(11, player.getInventory(), player);
        helper.assertTrue(menuClass.isInstance(menu), name + " must create " + menuClass.getSimpleName() + "; current=" + (menu == null ? "null" : menu.getClass().getName()));
        helper.assertTrue(menu.getType() == menuType, name + " menu must use registered menu type; current=" + menu.getType());
    }

    private static void assertBookRootAssets(GameTestHelper helper, String book) {
        com.google.gson.JsonElement appearance = readBookJsonResource(helper, book + "/appearance.json");
        helper.assertTrue(appearance.isJsonObject(), "Book appearance must be a JSON object for " + book);

        com.google.gson.JsonElement index = readBookJsonResource(helper, book + "/index.json");
        helper.assertTrue(index.isJsonArray() && !index.getAsJsonArray().isEmpty(),
            "Book index must be a non-empty JSON array for " + book);

        assertBookBinaryResource(helper, book + "/en_us/language.lang", 32);
    }

    private static com.google.gson.JsonElement readBookJsonResource(GameTestHelper helper, String path) {
        String fullPath = "assets/tconstruct/book/" + path;
        try (java.io.InputStream stream = TConstrictGameTests.class.getClassLoader().getResourceAsStream(fullPath)) {
            helper.assertTrue(stream != null, "Book JSON resource must be present on classpath: " + fullPath);
            try (java.io.Reader reader = new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)) {
                return com.google.gson.JsonParser.parseReader(reader);
            }
        } catch (java.io.IOException | com.google.gson.JsonParseException e) {
            helper.assertTrue(false, "Book JSON resource must parse cleanly: " + fullPath + "; error=" + e);
            return com.google.gson.JsonNull.INSTANCE;
        }
    }

    private static void assertBookBinaryResource(GameTestHelper helper, String path, int minBytes) {
        String fullPath = "assets/tconstruct/book/" + path;
        try (java.io.InputStream stream = TConstrictGameTests.class.getClassLoader().getResourceAsStream(fullPath)) {
            helper.assertTrue(stream != null, "Book resource must be present on classpath: " + fullPath);
            int size = stream.readAllBytes().length;
            helper.assertTrue(size >= minBytes,
                "Book resource must be packaged with expected content; path=" + fullPath + ", size=" + size + ", min=" + minBytes);
        } catch (java.io.IOException e) {
            helper.assertTrue(false, "Book resource must be readable: " + fullPath + "; error=" + e);
        }
    }

    private static void assertClassResource(GameTestHelper helper, String className, int minBytes) {
        readClassResourceBytes(helper, className, minBytes);
    }

    private static String readClassResourceText(GameTestHelper helper, String className, int minBytes) {
        return new String(readClassResourceBytes(helper, className, minBytes), java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    private static byte[] readClassResourceBytes(GameTestHelper helper, String className, int minBytes) {
        String fullPath = className.replace('.', '/') + ".class";
        try (java.io.InputStream stream = TConstrictGameTests.class.getClassLoader().getResourceAsStream(fullPath)) {
            helper.assertTrue(stream != null, "Class resource must be present on classpath: " + fullPath);
            byte[] bytes = stream.readAllBytes();
            int size = bytes.length;
            helper.assertTrue(size >= minBytes,
                "Class resource must be packaged with expected content; path=" + fullPath + ", size=" + size + ", min=" + minBytes);
            return bytes;
        } catch (java.io.IOException e) {
            helper.assertTrue(false, "Class resource must be readable: " + fullPath + "; error=" + e);
            return new byte[0];
        }
    }

    private static void assertSourceContains(GameTestHelper helper, String source, String token, String message) {
        helper.assertTrue(source.contains(token), message + "; missing token=" + token);
    }

    private static void buildMinimalHeatingStructure(ServerLevel level, BlockPos controllerPos, boolean seared) {
        BlockState controller = (seared ? TinkerSmeltery.smelteryController.get() : TinkerSmeltery.foundryController.get())
            .defaultBlockState()
            .setValue(ControllerBlock.FACING, Direction.NORTH);
        BlockState brick = (seared ? TinkerSmeltery.searedBricks.get() : TinkerSmeltery.scorchedBricks.get())
            .defaultBlockState();
        BlockState tank = (seared ? TinkerSmeltery.searedTank.get(TankType.FUEL_TANK) : TinkerSmeltery.scorchedTank.get(TankType.FUEL_TANK))
            .defaultBlockState();

        BlockPos min = controllerPos.offset(-1, -1, 0);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                level.setBlock(min.offset(x, 0, z), brick, Block.UPDATE_ALL);
            }
        }
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                if (x == 1 && z == 1) {
                    continue;
                }
                level.setBlock(min.offset(x, 1, z), brick, Block.UPDATE_ALL);
            }
        }
        level.setBlock(controllerPos, controller, Block.UPDATE_ALL);
        level.setBlock(controllerPos.offset(1, 0, 1), tank, Block.UPDATE_ALL);
    }

    private static <T> void assertRegistryEntry(GameTestHelper helper, ServerLevel level, ResourceKey<T> key, String name) {
        Registry<T> registry = level.registryAccess().registryOrThrow(ResourceKey.createRegistryKey(key.registry()));
        helper.assertTrue(registry.getHolder(key).isPresent(), name + " must be loaded in datapack registry; missing=" + key.location());
    }

    private static ConfiguredFeature<?, ?> getConfiguredFeature(GameTestHelper helper, ServerLevel level, ResourceKey<ConfiguredFeature<?, ?>> key, String name) {
        Registry<ConfiguredFeature<?, ?>> registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        var holder = registry.getHolder(key).orElse(null);
        helper.assertTrue(holder != null, name + " must be loaded in configured feature registry; missing=" + key.location());
        return holder.value();
    }

    private static <T> T roundTrip(ServerLevel level, StreamCodec<RegistryFriendlyByteBuf, T> codec, T packet) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        codec.encode(buffer, packet);
        T decoded = codec.decode(buffer);
        if (buffer.readableBytes() != 0) {
            throw new IllegalStateException("Packet codec left unread bytes: " + buffer.readableBytes() + " for " + packet.getClass().getName());
        }
        return decoded;
    }

    private static <T> T getPrivateField(Object object, String fieldName, Class<T> fieldType) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return fieldType.cast(field.get(object));
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new AssertionError("Failed reading " + fieldName + " from " + object.getClass().getName(), e);
        }
    }

    private static void fillBox(ServerLevel level, BlockPos min, BlockPos max, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }

    private static void primeWorldgenHeightmaps(ServerLevel level, BlockPos min, BlockPos max) {
        int minChunkX = Math.floorDiv(min.getX(), 16);
        int maxChunkX = Math.floorDiv(max.getX(), 16);
        int minChunkZ = Math.floorDiv(min.getZ(), 16);
        int maxChunkZ = Math.floorDiv(max.getZ(), 16);
        EnumSet<Heightmap.Types> worldgenMaps = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Heightmap.primeHeightmaps(level.getChunk(chunkX, chunkZ), worldgenMaps);
            }
        }
    }

    private static int countBlock(ServerLevel level, BlockPos min, BlockPos max, Block block) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(pos).is(block)) {
                count++;
            }
        }
        return count;
    }

    private static int countItems(ServerLevel level, BlockPos pos, net.minecraft.world.item.Item item) {
        int count = 0;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3))) {
            ItemStack stack = entity.getItem();
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int countInventoryItems(net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void assertTinkerStationRecipeLoaded(GameTestHelper helper, ServerLevel level, String recipePath, String name) {
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("tconstruct", recipePath);
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.TINKER_STATION.get()).stream()
                .anyMatch(holder -> holder.id().equals(recipeId)),
            name + " must be loaded; missing=" + recipeId);
    }

    private static void assertCraftingRecipeLoaded(GameTestHelper helper, ServerLevel level, String recipePath, String name) {
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("tconstruct", recipePath);
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                .anyMatch(holder -> holder.id().equals(recipeId)),
            name + " must be loaded; missing=" + recipeId);
    }

    private static void assertModifierWorktableRecipeLoaded(GameTestHelper helper, ServerLevel level, String recipePath, String name) {
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("tconstruct", recipePath);
        helper.assertTrue(level.getRecipeManager().getAllRecipesFor(TinkerRecipeTypes.MODIFIER_WORKTABLE.get()).stream()
                .anyMatch(holder -> holder.id().equals(recipeId)),
            name + " must be loaded; missing=" + recipeId);
    }

    private static FancyItemFrameEntity makeFancyFrame(ServerLevel level, BlockPos pos, Direction direction, FrameType type) {
        BlockPos support = pos.relative(direction.getOpposite());
        level.setBlock(support, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        FancyItemFrameEntity frame = new FancyItemFrameEntity(level, pos, direction, type);
        level.addFreshEntity(frame);
        return frame;
    }

    private static void tickEntity(Entity entity, int ticks) {
        for (int i = 0; i < ticks; i++) {
            entity.tick();
        }
    }

    private static void setFancyFrameRotation(FancyItemFrameEntity frame, int rotation) {
        try {
            var method = FancyItemFrameEntity.class.getDeclaredMethod("setRotation", int.class, boolean.class);
            method.setAccessible(true);
            method.invoke(frame, rotation, true);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to invoke fancy item frame rotation setter", e);
        }
    }

    private static void setThrownToolDealtDamage(ThrownTool projectile) {
        try {
            Field field = ThrownTool.class.getDeclaredField("tconstructDealtDamage");
            field.setAccessible(true);
            field.setBoolean(projectile, true);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to mark thrown tool as post-impact", e);
        }
    }

    private static void assertFloatEquals(GameTestHelper helper, float expected, float actual, float epsilon, String message) {
        helper.assertTrue(Math.abs(expected - actual) <= epsilon,
            message + "; expected=" + expected + ", actual=" + actual);
    }

    private static void assertShieldBasics(GameTestHelper helper, ItemStack stack, ModifiableItem item, float blockAmount, float blockAngle, float useSpeed,
                                          int upgradeSlots, int defenseSlots, int abilitySlots) {
        helper.assertTrue(stack.is(item), "Shield build must return the expected registered item; expected=" + item + ", stack=" + stack);
        helper.assertTrue(stack.is(TinkerTags.Items.SHIELDS), "TConstruct shield must be in the modifiable shield tag; stack=" + stack);
        helper.assertTrue(stack.is(TinkerTags.Items.HELD_ARMOR), "TConstruct shield must be treated as held armor/offhand equipment; stack=" + stack);
        helper.assertTrue(stack.is(Tags.Items.TOOLS_SHIELD), "TConstruct shield must be exposed through the NeoForge shield tool tag; stack=" + stack);

        ToolStack tool = ToolStack.from(stack);
        helper.assertTrue(tool.getMaterials().size() >= 1, "TConstruct shield must persist material entries; materials=" + tool.getMaterials());
        helper.assertTrue(tool.getModifiers().getLevel(ModifierIds.blocking) == 1,
            "TConstruct shield must keep the upstream blocking trait; modifiers=" + tool.getModifierList());
        assertFloatEquals(helper, blockAmount, tool.getStats().get(ToolStats.BLOCK_AMOUNT), 0.0001F,
            "TConstruct shield must keep upstream block amount");
        assertFloatEquals(helper, blockAngle, tool.getStats().get(ToolStats.BLOCK_ANGLE), 0.0001F,
            "TConstruct shield must keep upstream block angle");
        assertFloatEquals(helper, useSpeed, tool.getStats().get(ToolStats.USE_ITEM_SPEED), 0.0001F,
            "TConstruct shield must keep upstream use item speed");
        helper.assertTrue(tool.getStats().get(ToolStats.DURABILITY) > 0,
            "TConstruct shield must expose positive durability stats; stats=" + tool.getStats());
        helper.assertTrue(tool.getFreeSlots(SlotType.UPGRADE) == upgradeSlots,
            "TConstruct shield must keep upstream upgrade slots; expected=" + upgradeSlots + ", actual=" + tool.getFreeSlots(SlotType.UPGRADE));
        helper.assertTrue(tool.getFreeSlots(SlotType.DEFENSE) == defenseSlots,
            "TConstruct shield must keep upstream defense slots; expected=" + defenseSlots + ", actual=" + tool.getFreeSlots(SlotType.DEFENSE));
        helper.assertTrue(tool.getFreeSlots(SlotType.ABILITY) == abilitySlots,
            "TConstruct shield must keep upstream ability slots; expected=" + abilitySlots + ", actual=" + tool.getFreeSlots(SlotType.ABILITY));
        helper.assertTrue(tool.tryValidate() == null, "TConstruct shield built from valid materials must validate; error=" + tool.tryValidate());
        helper.assertTrue(item.getEquipmentSlot(stack) == EquipmentSlot.OFFHAND,
            "TConstruct shield must equip in the offhand slot; slot=" + item.getEquipmentSlot(stack));
        helper.assertTrue(item.canPerformAction(stack, ItemAbilities.SHIELD_BLOCK),
            "TConstruct shield must support NeoForge SHIELD_BLOCK action");
    }

    private static boolean playerHasModifierCrystal(net.minecraft.world.entity.player.Inventory inventory, slimeknights.tconstruct.library.modifiers.ModifierId modifier) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(TinkerModifiers.modifierCrystal.get()) && modifier.equals(ModifierCrystalItem.getModifier(stack))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static FuelInfo findFuelInfo(List<FuelInfo> fuels, Fluid fluid) {
        for (FuelInfo info : fuels) {
            if (info.getFluid().getFluid() == fluid) {
                return info;
            }
        }
        return null;
    }

    private static List<ItemStack> getBlockDrops(ServerLevel level, BlockState state, BlockPos pos, ItemStack tool) {
        return state.getDrops(new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withParameter(LootContextParams.TOOL, tool));
    }

    private static LootContext makeLootContext(ServerLevel level, BlockPos pos, BlockState state, ItemStack tool) {
        LootParams params = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withParameter(LootContextParams.BLOCK_STATE, state)
            .withParameter(LootContextParams.TOOL, tool)
            .create(LootContextParamSets.BLOCK);
        try {
            var constructor = LootContext.class.getDeclaredConstructor(LootParams.class, RandomSource.class, HolderGetter.Provider.class);
            constructor.setAccessible(true);
            return constructor.newInstance(params, level.getRandom(), level.getServer().reloadableRegistries().lookup());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create loot context for autosmelt test", e);
        }
    }

    private static ItemStack buildIronExcavator() {
        return ToolBuildHandler.buildItemFromMaterials(TinkerTools.excavator.get(), MaterialNBT.builder()
            .add(MaterialIds.iron)
            .add(MaterialIds.iron)
            .add(MaterialIds.iron)
            .add(MaterialIds.iron)
            .build());
    }

    private static void placeLoadedDispenser(GameTestHelper helper, ServerLevel level, BlockPos pos, ItemStack stack) {
        level.setBlock(pos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        helper.assertTrue(blockEntity instanceof DispenserBlockEntity,
            "Test dispenser must create a dispenser block entity; entity=" + blockEntity);
        ((DispenserBlockEntity)blockEntity).setItem(0, stack);
    }

    private static void dispenseLoadedDispenser(ServerLevel level, BlockPos pos) {
        try {
            var method = DispenserBlock.class.getDeclaredMethod("dispenseFrom", ServerLevel.class, BlockState.class, BlockPos.class);
            method.setAccessible(true);
            method.invoke(Blocks.DISPENSER, level, level.getBlockState(pos), pos);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to invoke vanilla dispenser dispense path", e);
        }
    }

    private static void assertDispenserSlotCount(GameTestHelper helper, ServerLevel level, BlockPos pos, int count, String name) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        helper.assertTrue(blockEntity instanceof DispenserBlockEntity,
            name + " must keep a dispenser block entity after firing; entity=" + blockEntity);
        ItemStack remaining = ((DispenserBlockEntity)blockEntity).getItem(0);
        helper.assertTrue(remaining.getCount() == count,
            name + " must consume exactly one item from slot 0; remaining=" + remaining);
    }

    private static ItemStack buildIronPickaxe() {
        return ToolBuildHandler.buildItemFromMaterials(TinkerTools.pickaxe.get(), MaterialNBT.builder()
            .add(MaterialIds.iron)
            .add(MaterialIds.wood)
            .add(MaterialIds.iron)
            .build());
    }

    private static ItemStack buildIronPickaxeWithModifier(ModifierId modifier) {
        ToolStack tool = ToolStack.from(buildIronPickaxe());
        tool.getPersistentData().setSlots(SlotType.ABILITY, 3);
        tool.addModifier(modifier, 1);
        return tool.createStack();
    }

    private static InteractionResult useHeldToolOn(Player player, BlockPos pos, Direction face) {
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false));
        if (stack.getItem() instanceof ModifiableItem modifiable) {
            InteractionResult result = modifiable.onItemUseFirst(stack, context);
            return result.consumesAction() ? result : modifiable.useOn(context);
        }
        InteractionResult result = stack.onItemUseFirst(context);
        return result.consumesAction() ? result : stack.useOn(context);
    }

    private static ItemStack buildIronKama() {
        return ToolBuildHandler.buildItemFromMaterials(TinkerTools.kama.get(), MaterialNBT.builder()
            .add(MaterialIds.iron)
            .add(MaterialIds.wood)
            .add(MaterialIds.iron)
            .build());
    }

    private static ItemStack buildBasicFishingRod() {
        return ToolBuildHandler.buildItemFromMaterials(TinkerTools.fishingRod.get(), MaterialNBT.builder()
            .add(MaterialIds.wood)
            .add(MaterialIds.string)
            .add(MaterialIds.flint)
            .build());
    }

    private static class TestCombatFishingHook extends CombatFishingHook {
        TestCombatFishingHook(Entity owner, ServerLevel level) {
            super(TinkerTools.fishingHook.get(), level);
            setOwner(owner);
        }

        boolean exposesCanHitEntity(Entity target) {
            return canHitEntity(target);
        }

        void exposePullEntity(Entity target) {
            pullEntity(target);
        }
    }
}
