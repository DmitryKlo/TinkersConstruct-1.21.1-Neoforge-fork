package slimeknights.tconstruct.fluids;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.fluids.item.ContainerFoodItem.FluidContainerFoodItem;
import slimeknights.tconstruct.fluids.item.MagmaBottleItem;
import slimeknights.tconstruct.fluids.item.PotionBucketItem;
import slimeknights.tconstruct.fluids.util.ConstantFluidContainerWrapper;
import slimeknights.tconstruct.shared.block.SlimeType;

/** Mod bus fluid capability registration */
@SuppressWarnings("unused")
@EventBusSubscriber(modid = TConstruct.MOD_ID, bus = Bus.MOD)
public class FluidEvents {
  @SubscribeEvent
  static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerItem(
      Capabilities.FluidHandler.ITEM,
      (stack, ctx) -> new ConstantFluidContainerWrapper(new FluidStack(TinkerFluids.powderedSnow.get(), FluidType.BUCKET_VOLUME), stack, Items.BUCKET.getDefaultInstance()),
      Items.POWDER_SNOW_BUCKET
    );

    registerFluidFoodItem(event, TinkerFluids.venomBottle.get());
    registerFluidFoodItem(event, TinkerFluids.magmaBottle.get());
    for (SlimeType type : SlimeType.values()) {
      registerFluidFoodItem(event, TinkerFluids.slimeBottle.get(type));
    }

    Item potionBucket = TinkerFluids.potion.asItem();
    if (potionBucket instanceof PotionBucketItem bucket) {
      event.registerItem(Capabilities.FluidHandler.ITEM, bucket::createFluidHandler, potionBucket);
    }
  }

  private static void registerFluidFoodItem(RegisterCapabilitiesEvent event, Item item) {
    if (item instanceof FluidContainerFoodItem foodItem) {
      event.registerItem(Capabilities.FluidHandler.ITEM, foodItem::createFluidHandler, item);
    } else if (item instanceof MagmaBottleItem magmaBottle) {
      event.registerItem(Capabilities.FluidHandler.ITEM, magmaBottle::createFluidHandler, item);
    }
  }
}
