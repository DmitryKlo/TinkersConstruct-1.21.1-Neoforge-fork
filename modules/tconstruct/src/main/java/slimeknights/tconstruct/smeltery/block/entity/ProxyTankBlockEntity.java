package slimeknights.tconstruct.smeltery.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.mantle.fluid.FluidTransferHelper;
import slimeknights.tconstruct.library.fluid.IFluidTankUpdater;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.tank.ProxyItemTank;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/** Block entity with a tank that proxies to the nested item handler */
public class ProxyTankBlockEntity extends MantleBlockEntity implements IFluidTankUpdater {
  @Getter
  private final ProxyItemTank<ProxyTankBlockEntity> itemTank = new ProxyItemTank<>(this);
  private int lastStrength = -1;

  protected ProxyTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  public ProxyTankBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.proxyTank.get(), pos, state);
  }

  private int calculateComparatorStrength() {
    int capacity = itemTank.getTankCapacity(0);
    if (capacity == 0) {
      return 0;
    }
    return 1 + 14 * itemTank.getFluidInTank(0).getAmount() / capacity;
  }

  public int getComparatorStrength() {
    if (lastStrength == -1) {
      lastStrength = calculateComparatorStrength();
    }
    return lastStrength;
  }

  @Override
  public void onTankContentsChanged() {
    if (level != null && !level.isClientSide) {
      setChangedFast();
      int newStrength = calculateComparatorStrength();
      if (newStrength != lastStrength) {
        lastStrength = newStrength;
        level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
      }
    }
  }

  public void interact(Player player, InteractionHand hand, boolean clickedTank) {
    if (level == null || level.isClientSide) {
      return;
    }

    ItemStack held = player.getItemInHand(hand);
    ItemStack inventory = itemTank.getStack();
    if (!inventory.isEmpty()) {
      if (!held.isEmpty() && FluidTransferHelper.interactWithContainer(level, worldPosition, itemTank, player, hand).didTransfer()
        || FluidTransferHelper.interactWithFilledBucket(level, worldPosition, itemTank, player, hand, getBlockState().getValue(HORIZONTAL_FACING)).didTransfer()) {
        return;
      }
      if (clickedTank) {
        return;
      }
    }
    if (inventory.isEmpty()) {
      if (!held.isEmpty() && itemTank.isItemValid(0, held)) {
        ItemStack stack = held.split(itemTank.getSlotLimit(0));
        player.setItemInHand(hand, held.isEmpty() ? ItemStack.EMPTY : held);
        itemTank.setStack(stack);
      }
    } else if (held.isEmpty()) {
      player.setItemInHand(hand, inventory);
      itemTank.setStack(ItemStack.EMPTY);
    } else {
      inventory = inventory.copy();
      player.addItem(inventory);
      itemTank.setStack(inventory);
    }
  }

  private static final String TAG_ITEM = "item";

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    if (tag.contains(TAG_ITEM, Tag.TAG_COMPOUND)) {
      itemTank.readFromNBT(tag.getCompound(TAG_ITEM));
    }
  }

  @Override
  protected void saveSynced(CompoundTag tag) {
    super.saveSynced(tag);
    tag.put(TAG_ITEM, itemTank.writeToNBT());
  }
}
