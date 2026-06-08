package slimeknights.tconstruct.smeltery.block.entity.controller;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.block.entity.NameableBlockEntity;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.client.model.ModelProperties;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.controller.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.controller.MelterBlock;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity.ITankInventoryBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.MeltingModuleInventory;
import slimeknights.tconstruct.smeltery.block.entity.module.SolidFuelModule;
import slimeknights.tconstruct.smeltery.menu.MelterContainerMenu;

import javax.annotation.Nullable;

public class MelterBlockEntity extends NameableBlockEntity implements ITankInventoryBlockEntity {

  /** Max capacity for the tank */
  private static final int TANK_CAPACITY = FluidValues.INGOT * 24;
  private static final String TAG_INVENTORY = "inventory";
  private static final MutableComponent NAME = TConstruct.makeTranslation("gui", "melter");

  public static final BlockEntityTicker<MelterBlockEntity> SERVER_TICKER = (level, pos, state, self) -> self.tick(level, pos, state);

  @Getter
  protected final FluidTankAnimated tank = new FluidTankAnimated(TANK_CAPACITY, this);
  @Getter @Setter
  private int lastStrength = -1;

  private int tick;
  @Getter
  private final MeltingModuleInventory meltingInventory = new MeltingModuleInventory(this, tank, Config.COMMON.melterOreRate, 3);
  @Getter
  private final SolidFuelModule fuelModule;

  public MelterBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.melter.get(), pos, state);
  }

  @SuppressWarnings("WeakerAccess")
  protected MelterBlockEntity(BlockEntityType<? extends MelterBlockEntity> type, BlockPos pos, BlockState state) {
    super(type, pos, state, NAME);
    this.fuelModule = new SolidFuelModule(this, pos.below());
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inv, Player playerEntity) {
    return new MelterContainerMenu(id, inv, this);
  }

  @Override
  public @NotNull ModelData getModelData() {
    return ModelData.builder()
                    .with(ModelProperties.FLUID_STACK, tank.getFluid())
                    .with(ModelProperties.TANK_CAPACITY, tank.getCapacity()).build();
  }

  private boolean isFormed() {
    BlockState state = getBlockState();
    return state.hasProperty(MelterBlock.IN_STRUCTURE) && state.getValue(MelterBlock.IN_STRUCTURE);
  }

  private void tick(Level level, BlockPos pos, BlockState state) {
    if (isFormed()) {
      switch (tick) {
        case 0 -> {
          if (!fuelModule.hasFuel() && meltingInventory.canHeat(fuelModule.findFuel(false))) {
            fuelModule.findFuel(true);
          }
        }
        case 2 -> {
          boolean hasFuel = fuelModule.hasFuel();
          if (state.getValue(ControllerBlock.ACTIVE) != hasFuel) {
            level.setBlockAndUpdate(pos, state.setValue(ControllerBlock.ACTIVE, hasFuel));
            BlockPos down = pos.below();
            BlockState downState = level.getBlockState(down);
            if (downState.is(TinkerTags.Blocks.FUEL_TANKS) && downState.hasProperty(ControllerBlock.ACTIVE) && downState.getValue(ControllerBlock.ACTIVE) != hasFuel) {
              level.setBlockAndUpdate(down, downState.setValue(ControllerBlock.ACTIVE, hasFuel));
            }
          }
          if (hasFuel) {
            meltingInventory.heatItems(fuelModule.getTemperature(), fuelModule.getRate());
            fuelModule.decreaseFuel(1);
          } else {
            meltingInventory.coolItems();
          }
        }
      }
    } else if (tick == 2) {
      if (fuelModule.hasFuel()) {
        fuelModule.decreaseFuel(1);
      } else {
        meltingInventory.coolItems();
      }
    }
    tick = (tick + 1) % 4;
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    tank.readFromNBT(registries, tag.getCompound(NBTTags.TANK));
    fuelModule.readFromTag(tag);
    if (tag.contains(TAG_INVENTORY, Tag.TAG_COMPOUND)) {
      meltingInventory.readFromTag(tag.getCompound(TAG_INVENTORY));
    }
  }

  @Override
  public void saveSynced(CompoundTag tag) {
    super.saveSynced(tag);
    tag.put(NBTTags.TANK, tank.writeToNBT(level.registryAccess(), new CompoundTag()));
    tag.put(TAG_INVENTORY, meltingInventory.writeToTag());
  }

  @Override
  public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    fuelModule.writeToTag(tag);
  }

  @Override
  public IItemHandler getItemHandler() {
    return meltingInventory;
  }
}
