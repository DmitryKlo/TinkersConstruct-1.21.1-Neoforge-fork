package slimeknights.tconstruct.smeltery.block.entity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.component.DuctBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.SmelteryInputOutputBlockEntity.ChuteBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.SmelteryInputOutputBlockEntity.SmelteryFluidIO;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.AlloyerBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.MelterBlockEntity;
import slimeknights.tconstruct.smeltery.item.CopperCanItem;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.smeltery.item.TankItemFluidHandler;

/** Registers smeltery block and item capabilities for NeoForge */
public final class SmelteryBlockEntityCapabilities {
  private SmelteryBlockEntityCapabilities() {}

  public static void register(RegisterCapabilitiesEvent event) {
    TankBlockEntity.registerCapabilities(event);

    var fluidHandler = Capabilities.FluidHandler.BLOCK;
    var itemHandler = Capabilities.ItemHandler.BLOCK;

    event.registerBlockEntity(fluidHandler, TinkerSmeltery.melter.get(), (be, side) -> be.getTank());
    event.registerBlockEntity(itemHandler, TinkerSmeltery.melter.get(), (be, side) -> be.getItemHandler());

    event.registerBlockEntity(fluidHandler, TinkerSmeltery.alloyer.get(), (be, side) -> be.getTank());

    event.registerBlockEntity(itemHandler, TinkerSmeltery.smeltery.get(), (be, side) -> be.getMeltingInventory());
    event.registerBlockEntity(itemHandler, TinkerSmeltery.foundry.get(), (be, side) -> be.getMeltingInventory());

    event.registerBlockEntity(fluidHandler, TinkerSmeltery.drain.get(), (be, side) -> be.getFluidHandler());
    event.registerBlockEntity(fluidHandler, TinkerSmeltery.duct.get(), (be, side) -> be.getFluidHandler());
    event.registerBlockEntity(itemHandler, TinkerSmeltery.duct.get(), (be, side) -> be.getItemHandler());

    event.registerBlockEntity(itemHandler, TinkerSmeltery.chute.get(), (be, side) -> be.getItemHandler());

    event.registerBlockEntity(itemHandler, TinkerSmeltery.heater.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(itemHandler, TinkerSmeltery.fluidCannon.get(), (be, side) -> be.getItemHandler());

    event.registerBlockEntity(fluidHandler, TinkerSmeltery.channel.get(), ChannelBlockEntity::getFluidHandler);
    event.registerBlockEntity(fluidHandler, TinkerSmeltery.basin.get(), (be, side) -> be.getTank());
    event.registerBlockEntity(fluidHandler, TinkerSmeltery.table.get(), (be, side) -> be.getTank());
    event.registerBlockEntity(fluidHandler, TinkerSmeltery.castingTank.get(), (be, side) -> be.getTank());

    event.registerBlockEntity(itemHandler, TinkerSmeltery.proxyTank.get(), (be, side) -> be.getItemTank());
    event.registerBlockEntity(fluidHandler, TinkerSmeltery.proxyTank.get(), (be, side) -> be.getItemTank());

    TankItem.registerCapabilities(event);
    event.registerItem(Capabilities.FluidHandler.ITEM, CopperCanItem::createFluidHandler, TinkerSmeltery.copperCan.get());
  }
}
