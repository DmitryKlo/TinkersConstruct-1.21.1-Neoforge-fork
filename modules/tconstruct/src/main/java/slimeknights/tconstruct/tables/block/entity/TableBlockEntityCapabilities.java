package slimeknights.tconstruct.tables.block.entity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.entity.chest.AbstractChestBlockEntity;

/** Registers table block entity item capabilities for NeoForge */
public final class TableBlockEntityCapabilities {
  private TableBlockEntityCapabilities() {}

  public static void register(RegisterCapabilitiesEvent event) {
    var itemHandler = Capabilities.ItemHandler.BLOCK;

    event.registerBlockEntity(itemHandler, TinkerTables.craftingStationTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(itemHandler, TinkerTables.tinkerStationTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(itemHandler, TinkerTables.partBuilderTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(itemHandler, TinkerTables.modifierWorktableTile.get(), (be, side) -> be.getItemHandler());

    event.registerBlockEntity(itemHandler, TinkerTables.tinkersChestTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(itemHandler, TinkerTables.partChestTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(itemHandler, TinkerTables.castChestTile.get(), (be, side) -> be.getItemHandler());
  }
}
