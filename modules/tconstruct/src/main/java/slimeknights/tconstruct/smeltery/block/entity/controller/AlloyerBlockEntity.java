package slimeknights.tconstruct.smeltery.block.entity.controller;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.block.entity.NameableBlockEntity;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.smeltery.block.controller.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.controller.MelterBlock;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.SolidFuelModule;
import slimeknights.tconstruct.smeltery.block.entity.module.alloying.MixerAlloyTank;
import slimeknights.tconstruct.smeltery.block.entity.module.alloying.SingleAlloyingModule;
import slimeknights.tconstruct.smeltery.menu.AlloyerContainerMenu;

import javax.annotation.Nullable;

/**
 * Dedicated alloying block
 */
public class AlloyerBlockEntity extends NameableBlockEntity implements ITankBlockEntity {
  private static final int TANK_CAPACITY = TankType.INGOT_TANK.getCapacity();
  private static final Component NAME = TConstruct.makeTranslation("gui", "alloyer");

  public static final BlockEntityTicker<AlloyerBlockEntity> SERVER_TICKER = (level, pos, state, self) -> self.tick(level, pos, state);

  @Getter
  protected final FluidTankAnimated tank = new FluidTankAnimated(TANK_CAPACITY, this);
  @Getter
  private final MixerAlloyTank alloyTank = new MixerAlloyTank(this, tank);
  private final SingleAlloyingModule alloyingModule = new SingleAlloyingModule(this, alloyTank);
  @Getter
  private final SolidFuelModule fuelModule;
  @Getter @Setter
  private int lastStrength = -1;

  private int tick;

  public AlloyerBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.alloyer.get(), pos, state);
  }

  protected AlloyerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state, NAME);
    this.fuelModule = new SolidFuelModule(this, pos.below());
  }

  private boolean isFormed() {
    BlockState state = getBlockState();
    return state.hasProperty(MelterBlock.IN_STRUCTURE) && state.getValue(MelterBlock.IN_STRUCTURE);
  }

  private void tick(Level level, BlockPos pos, BlockState state) {
    if (isFormed()) {
      switch (tick) {
        case 0 -> {
          alloyTank.setTemperature(fuelModule.findFuel(false));
          if (!fuelModule.hasFuel() && alloyingModule.canAlloy()) {
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
            alloyTank.setTemperature(fuelModule.getTemperature());
            alloyingModule.doAlloy();
            fuelModule.decreaseFuel(1);
          }
        }
      }
    } else if (tick == 2 && fuelModule.hasFuel()) {
      fuelModule.decreaseFuel(1);
    }
    tick = (tick + 1) % 4;
  }

  public void neighborChanged(net.minecraft.core.Direction side) {
    alloyTank.refresh(side, true);
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inv, Player playerEntity) {
    return new AlloyerContainerMenu(id, inv, this);
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void saveSynced(CompoundTag tag) {
    super.saveSynced(tag);
    tag.put(NBTTags.TANK, tank.writeToNBT(level.registryAccess(), new CompoundTag()));
  }

  @Override
  public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    fuelModule.writeToTag(tag);
  }

  @Override
  protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
    super.loadAdditional(nbt, registries);
    tank.readFromNBT(registries, nbt.getCompound(NBTTags.TANK));
    fuelModule.readFromTag(nbt);
  }
}
