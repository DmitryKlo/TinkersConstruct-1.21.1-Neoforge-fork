package slimeknights.tconstruct.tables.block.entity.chest;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import slimeknights.mantle.block.entity.NameableBlockEntity;
import slimeknights.tconstruct.tables.block.entity.inventory.IChestItemHandler;
import slimeknights.tconstruct.tables.menu.TinkerChestContainerMenu;

import javax.annotation.Nullable;

/** Shared base logic for all Tinkers' chest tile entities */
public abstract class AbstractChestBlockEntity extends NameableBlockEntity {
  private static final String KEY_ITEMS = "Items";

  @Getter
  private final IChestItemHandler itemHandler;

  protected AbstractChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component name, IChestItemHandler itemHandler) {
    super(type, pos, state, name);
    itemHandler.setParent(this);
    this.itemHandler = itemHandler;
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new TinkerChestContainerMenu(menuId, playerInventory, this);
  }

  public boolean canInsert(Player player, ItemStack heldItem) {
    return true;
  }

  @Override
  public void saveAdditional(CompoundTag tags, HolderLookup.Provider registries) {
    super.saveAdditional(tags, registries);
    CompoundTag handlerNBT = itemHandler.serializeNBT(registries);
    tags.put(KEY_ITEMS, handlerNBT.getList(KEY_ITEMS, Tag.TAG_COMPOUND));
  }

  public void readInventory(CompoundTag tags, HolderLookup.Provider registries) {
    CompoundTag handlerNBT = new CompoundTag();
    handlerNBT.put(KEY_ITEMS, tags.getList(KEY_ITEMS, Tag.TAG_COMPOUND));
    itemHandler.deserializeNBT(registries, handlerNBT);
  }

  @Override
  protected void loadAdditional(CompoundTag tags, HolderLookup.Provider registries) {
    super.loadAdditional(tags, registries);
    readInventory(tags, registries);
  }
}
