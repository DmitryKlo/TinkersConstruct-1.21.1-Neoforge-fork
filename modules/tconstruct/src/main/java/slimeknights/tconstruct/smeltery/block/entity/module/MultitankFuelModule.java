package slimeknights.tconstruct.smeltery.block.entity.module;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import slimeknights.mantle.block.entity.MantleBlockEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

/** Fuel module that supports multiple tanks, selecting just one for the fuel result */
public class MultitankFuelModule extends FuelModule implements IFluidHandler {
  /** Block position that will never be valid in world, used for sync */
  private static final BlockPos NULL_POS = new BlockPos(0, Short.MIN_VALUE, 0);

  /** Supplier for the list of valid tank positions */
  private final Supplier<List<BlockPos>> tankSupplier;
  /** Position of the last fluid handler */
  private BlockPos lastPos = NULL_POS;

  /** Map of all tank handlers at each relevant position. Used for fast switching between handlers, notably in the UI */
  private Map<BlockPos, IFluidHandler> tankHandlers;
  /** Last non-empty tank count seen by the client, used to keep the fuel GUI stable during structure resyncs. */
  private int knownTankCount;

  public MultitankFuelModule(MantleBlockEntity parent, Supplier<List<BlockPos>> tankSupplier) {
    super(parent);
    this.tankSupplier = tankSupplier;
  }

  /** Resets just the last fluid handler */
  private void clearLastHandler() {
    lastPos = NULL_POS;
    super.resetHandler();
  }

  @Override
  protected void resetHandler() {
    lastPos = NULL_POS;
    super.resetHandler();
  }

  /** Called on structure rebuild to clear the gui handler list */
  public void clearFluidListeners() {
    tankHandlers = null;
    // Structure updates invalidate cached capabilities, but the last known tank position is synced separately.
    super.resetHandler();
  }

  /** Called on servant load to ensure the handler is present in the cache */
  public void ensureTankPresent(BlockEntity be) {
    BlockPos pos = be.getBlockPos();
    if (tankHandlers != null && !tankHandlers.containsKey(pos)) {
      Level level = getLevel();
      IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, be.getBlockState(), be, null);
      if (handler != null) {
        tankHandlers.put(pos, handler);
      }
    }
  }

  /** Gets the map from position to fluid handler */
  private Map<BlockPos, IFluidHandler> getTankHandlers() {
    if (tankHandlers == null) {
      tankHandlers = new LinkedHashMap<>();
      Level world = getLevel();
      for (BlockPos pos : tankSupplier.get()) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null) {
          BlockState state = te.getBlockState();
          IFluidHandler handler = world.getCapability(Capabilities.FluidHandler.BLOCK, pos, state, te, null);
          if (handler != null) {
            tankHandlers.put(pos, handler);
          }
        }
      }
      if (!tankHandlers.isEmpty()) {
        knownTankCount = tankHandlers.size();
      }
    }
    return tankHandlers;
  }


  /* Fuel finding */

  /**
   * Tries to consume fuel from the given position
   * @param pos  Position
   * @return   Temperature of the consumed fuel, 0 if none found
   */
  private int tryFuelPosition(BlockPos pos, boolean consume) {
    IFluidHandler tankCap = getTankHandlers().get(pos);
    if (tankCap != null) {
      int temperature = tryLiquidFuel(tankCap, consume);
      if (temperature > 0) {
        clearLastHandler();
        fluidHandler = tankCap;
        lastPos = pos;
        return temperature;
      }
    }
    return 0;
  }

  /**
   * Attempts to consume fuel from one of the tanks
   * @return  temperature of the found fluid, 0 if none
   */
  @Override
  public int findFuel(boolean consume) {
    if (fluidHandler != null) {
      int temperature = tryLiquidFuel(fluidHandler, consume);
      if (temperature > 0) {
        return temperature;
      }
    } else if (lastPos != NULL_POS) {
      int posTemp = tryFuelPosition(lastPos, consume);
      if (posTemp > 0) {
        return posTemp;
      }
    }

    for (BlockPos pos : tankSupplier.get()) {
      if (!pos.equals(lastPos)) {
        int posTemp = tryFuelPosition(pos, consume);
        if (posTemp > 0) {
          return posTemp;
        }
      }
    }

    if (consume) {
      temperature = 0;
      rate = 0;
    }
    return 0;
  }


  /* NBT */
  private static final String TAG_LAST_FUEL = "last_fuel";

  @Override
  public void readFromTag(CompoundTag nbt) {
    super.readFromTag(nbt);
    if (nbt.contains(TAG_LAST_FUEL)) {
      lastPos = NbtUtils.readBlockPos(nbt, TAG_LAST_FUEL).orElse(NULL_POS).offset(parent.getBlockPos());
    }
  }

  @Override
  public CompoundTag writeToTag(CompoundTag nbt) {
    nbt = super.writeToTag(nbt);
    if (lastPos != NULL_POS) {
      nbt.put(TAG_LAST_FUEL, NbtUtils.writeBlockPos(lastPos.subtract(parent.getBlockPos())));
    }
    return nbt;
  }


  /* UI syncing */
  private static final int LAST_X = 4;
  private static final int LAST_Y = 5;
  private static final int LAST_Z = 6;

  @Override
  public int getCount() {
    return 7;
  }

  @Override
  public int get(int index) {
    return switch (index) {
      case LAST_X -> lastPos.getX();
      case LAST_Y -> lastPos.getY();
      case LAST_Z -> lastPos.getZ();
      default -> super.get(index);
    };
  }

  @Override
  public void set(int index, int value) {
    if (LAST_X <= index && index <= LAST_Z) {
      switch (index) {
        case LAST_X -> lastPos = new BlockPos(value, lastPos.getY(), lastPos.getZ());
        case LAST_Y -> lastPos = new BlockPos(lastPos.getX(), value, lastPos.getZ());
        case LAST_Z -> lastPos = new BlockPos(lastPos.getX(), lastPos.getY(), value);
      }
      super.resetHandler();
    } else {
      super.set(index, value);
    }
  }

  @Override
  public FuelInfo getFuelInfo() {
    List<FuelInfo> infos = getFuelInfos();
    return infos.isEmpty() ? FuelInfo.EMPTY : infos.get(0);
  }

  @Override
  public List<FuelInfo> getFuelInfos() {
    List<IFluidHandler> handlers = getOrderedTankHandlers();
    if (handlers.isEmpty()) {
      return List.of();
    }

    Map<Fluid, FuelInfo> fuels = new LinkedHashMap<>();
    int totalCapacity = 0;
    for (IFluidHandler handler : handlers) {
      for (int tank = 0; tank < handler.getTanks(); tank++) {
        totalCapacity += handler.getTankCapacity(tank);
        FluidStack fluid = handler.getFluidInTank(tank);
        if (!fluid.isEmpty()) {
          var recipe = findRecipe(fluid.getFluid());
          if (recipe != null) {
            fuels.compute(fluid.getFluid(), (key, info) -> {
              if (info == null) {
                return FuelInfo.of(fluid, fluid.getAmount(), 0, recipe.getTemperature());
              }
              info.add(fluid.getAmount(), 0);
              return info;
            });
          }
        }
      }
    }

    if (fuels.isEmpty()) {
      return List.of();
    }
    for (FuelInfo info : fuels.values()) {
      info.setCapacity(totalCapacity);
    }
    return List.copyOf(fuels.values());
  }

  /** Gets tank handlers with the last used tank first */
  private List<IFluidHandler> getOrderedTankHandlers() {
    Map<BlockPos, IFluidHandler> handlers = getTankHandlers();
    if (handlers.isEmpty()) {
      return List.of();
    }

    List<IFluidHandler> ordered = new ArrayList<>(handlers.size());
    BlockPos mainTank = lastPos;
    if (mainTank.getY() == NULL_POS.getY()) {
      List<BlockPos> positions = tankSupplier.get();
      if (!positions.isEmpty()) {
        mainTank = positions.get(0);
      }
    }

    IFluidHandler mainHandler = fluidHandler != null ? fluidHandler : handlers.get(mainTank);
    if (mainHandler != null) {
      ordered.add(mainHandler);
    }
    for (Entry<BlockPos, IFluidHandler> entry : handlers.entrySet()) {
      IFluidHandler handler = entry.getValue();
      if (handler != null && handler != mainHandler) {
        ordered.add(handler);
      }
    }
    return ordered;
  }


  /* Fluid handler */

  /** Gets the most recently used fluid */
  public FluidStack getLastFluid() {
    if (fluidHandler != null) {
      return getFirstFluid(fluidHandler);
    }
    BlockPos pos;
    if (lastPos.getY() != NULL_POS.getY()) {
      pos = lastPos;
    } else {
      List<BlockPos> positions = tankSupplier.get();
      if (!positions.isEmpty()) {
        pos = positions.get(0);
      } else {
        return FluidStack.EMPTY;
      }
    }
    IFluidHandler handler = getTankHandlers().get(pos);
    return handler != null ? getFirstFluid(handler) : FluidStack.EMPTY;
  }

  /** Gets the first non-empty fluid in the handler */
  private static FluidStack getFirstFluid(IFluidHandler handler) {
    for (int tank = 0; tank < handler.getTanks(); tank++) {
      FluidStack fluid = handler.getFluidInTank(tank);
      if (!fluid.isEmpty()) {
        return fluid;
      }
    }
    return FluidStack.EMPTY;
  }

  @Override
  public int getTanks() {
    int count = tankSupplier.get().size();
    if (count > 0) {
      knownTankCount = count;
      return count;
    }
    if (tankHandlers != null && !tankHandlers.isEmpty()) {
      knownTankCount = tankHandlers.size();
      return knownTankCount;
    }
    return lastPos != NULL_POS ? Math.max(1, knownTankCount) : knownTankCount;
  }

  /** Gets the tank at the given index */
  private IFluidHandler getTank(int tank) {
    if (tank >= 0) {
      List<BlockPos> positions = tankSupplier.get();
      if (tank < positions.size()) {
        IFluidHandler handler = getTankHandlers().get(positions.get(tank));
        return handler != null ? handler : EmptyFluidHandler.INSTANCE;
      }
    }
    return EmptyFluidHandler.INSTANCE;
  }

  @Nonnull
  @Override
  public FluidStack getFluidInTank(int tank) {
    return getTank(tank).getFluidInTank(0);
  }

  @Override
  public int getTankCapacity(int tank) {
    return getTank(tank).getTankCapacity(0);
  }

  @Override
  public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
    return getTank(tank).isFluidValid(0, stack);
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    int totalFilled = 0;
    resource = resource.copy();
    for (IFluidHandler handler : getTankHandlers().values()) {
      int filled = handler.fill(resource, action);
      if (filled > 0) {
        totalFilled += filled;
        if (filled >= resource.getAmount()) {
          break;
        }
        if (totalFilled == filled) {
          resource = resource.copyWithAmount(resource.getAmount() - filled);
        } else {
          resource.shrink(filled);
        }
      }
    }
    return totalFilled;
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    FluidStack drainedSoFar = FluidStack.EMPTY;
    for (IFluidHandler handler : getTankHandlers().values()) {
      FluidStack drained = handler.drain(resource, action);
      if (!drained.isEmpty()) {
        if (drainedSoFar.isEmpty()) {
          drainedSoFar = drained;
          if (drained.getAmount() >= resource.getAmount()) {
            break;
          }
          resource = resource.copyWithAmount(resource.getAmount() - drained.getAmount());
        } else {
          drainedSoFar.grow(drained.getAmount());
          resource.shrink(drained.getAmount());
          if (resource.isEmpty()) {
            break;
          }
        }
      }
    }
    return drainedSoFar;
  }

  @Nonnull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    FluidStack drainedSoFar = FluidStack.EMPTY;
    FluidStack toDrain = FluidStack.EMPTY;
    for (IFluidHandler handler : getTankHandlers().values()) {
      if (toDrain.isEmpty()) {
        FluidStack drained = handler.drain(maxDrain, action);
        if (!drained.isEmpty()) {
          drainedSoFar = drained;
          if (drained.getAmount() >= maxDrain) {
            break;
          }
          toDrain = drained.copyWithAmount(maxDrain - drained.getAmount());
        }
      } else {
        FluidStack drained = handler.drain(toDrain, action);
        if (!drained.isEmpty()) {
          drainedSoFar.grow(drained.getAmount());
          toDrain.shrink(drained.getAmount());
          if (toDrain.isEmpty()) {
            break;
          }
        }
      }
    }
    return drainedSoFar;
  }
}
