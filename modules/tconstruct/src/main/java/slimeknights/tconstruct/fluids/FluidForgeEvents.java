package slimeknights.tconstruct.fluids;



import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.fml.common.EventBusSubscriber.Bus;

import slimeknights.tconstruct.TConstruct;



/** Forge bus fluid event handlers */

@EventBusSubscriber(modid = TConstruct.MOD_ID, bus = Bus.GAME)

public class FluidForgeEvents {

  @SubscribeEvent

  static void onFurnaceFuel(FurnaceFuelBurnTimeEvent event) {

    if (event.getItemStack().getItem() == TinkerFluids.blazingBlood.asItem()) {

      // 150% efficiency compared to lava bucket, compare to casting blaze rods, which cast into 120%

      event.setBurnTime(30000);

    }

  }

}

