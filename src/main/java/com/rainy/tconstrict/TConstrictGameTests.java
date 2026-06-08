package com.rainy.tconstrict;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.tools.definition.module.mining.IsEffectiveToolHook;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuelLookup;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.FaucetBlock;
import slimeknights.tconstruct.smeltery.block.component.OrientableSmelteryBlock;
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
import slimeknights.tconstruct.smeltery.menu.HeatingStructureContainerMenu;
import slimeknights.tconstruct.smeltery.menu.MelterContainerMenu;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

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

    private static ItemStack buildIronExcavator() {
        return ToolBuildHandler.buildItemFromMaterials(TinkerTools.excavator.get(), MaterialNBT.builder()
            .add(MaterialIds.iron)
            .add(MaterialIds.iron)
            .add(MaterialIds.iron)
            .add(MaterialIds.iron)
            .build());
    }
}
