package slimeknights.tconstruct.smeltery.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.network.FaucetActivationPacket;

import javax.annotation.Nullable;

import static slimeknights.tconstruct.smeltery.block.FaucetBlock.FACING;

public class FaucetBlockEntity extends MantleBlockEntity {
  /** amount of MB to extract from the input at a time */
  public static final int PACKET_SIZE = FluidValues.INGOT;
  /** Transfer rate of the faucet */
  public static final int MB_PER_TICK = FluidValues.NUGGET;

  public static final BlockEntityTicker<FaucetBlockEntity> SERVER_TICKER = (level, pos, world, self) -> self.tick();

  private static final String TAG_DRAINED = "drained";
  private static final String TAG_RENDER_FLUID = "render_fluid";
  private static final String TAG_STOP = "stop";
  private static final String TAG_STATE = "state";
  private static final String TAG_LAST_REDSTONE = "lastRedstone";

  /** If true, faucet is currently pouring */
  private FaucetState faucetState = FaucetState.OFF;
  /** If true, redstone told this faucet to stop, so stop when ready */
  private boolean stopPouring = false;
  /** Current fluid in the faucet */
  private FluidStack drained = FluidStack.EMPTY;
  /** Fluid for rendering, used to reduce the number of packets. There is a brief moment where {@link this#drained} is empty but we should be rendering something */
  @Getter
  private FluidStack renderFluid = FluidStack.EMPTY;
  /** Used for pulse detection */
  private boolean lastRedstoneState = false;

  /** Fluid handler of the input to the faucet */
  @Nullable
  private IFluidHandler inputHandler;
  /** Fluid handler of the output from the faucet */
  @Nullable
  private IFluidHandler outputHandler;

  public FaucetBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.faucet.get(), pos, state);
  }

  @SuppressWarnings("WeakerAccess")
  protected FaucetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }


  /* Fluid handler */

  @Nullable
  private IFluidHandler findFluidHandler(Direction side) {
    assert level != null;
    BlockPos neighborPos = worldPosition.relative(side);
    BlockEntity te = level.getBlockEntity(neighborPos);
    if (te != null) {
      return level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, te.getBlockState(), te, side.getOpposite());
    }
    return null;
  }

  @Nullable
  private IFluidHandler getInputHandler() {
    if (inputHandler == null) {
      inputHandler = findFluidHandler(getBlockState().getValue(FACING).getOpposite());
    }
    return inputHandler;
  }

  @Nullable
  private IFluidHandler getOutputHandler() {
    if (outputHandler == null) {
      outputHandler = findFluidHandler(Direction.DOWN);
    }
    return outputHandler;
  }

  public void neighborChanged(BlockPos neighbor) {
    if (worldPosition.equals(neighbor.above())) {
      outputHandler = null;
    } else if (worldPosition.equals(neighbor.relative(getBlockState().getValue(FACING)))) {
      inputHandler = null;
    }
  }


  /* Data */

  public boolean isPouring() {
    return faucetState != FaucetState.OFF;
  }

  public void activate() {
    if (level == null || level.isClientSide) {
      return;
    }
    switch (faucetState) {
      case OFF -> {
        stopPouring = false;
        doTransfer(true);
      }
      case POWERED -> {
        faucetState = FaucetState.OFF;
        syncToClient(FluidStack.EMPTY, false);
      }
      case POURING -> stopPouring = true;
    }
  }

  public void handleRedstone(boolean hasSignal) {
    if (hasSignal != lastRedstoneState) {
      lastRedstoneState = hasSignal;
      if (hasSignal) {
        if (level != null){
          level.scheduleTick(worldPosition, getBlockState().getBlock(), 2);
        }
      } else if (faucetState == FaucetState.POWERED) {
        faucetState = FaucetState.OFF;
        syncToClient(FluidStack.EMPTY, false);
      }
    }
  }


  /* Pouring */

  private void tick() {
    if (faucetState == FaucetState.OFF) {
      return;
    } else if (faucetState == FaucetState.POWERED && doTransfer(false)) {
      faucetState = FaucetState.POURING;
      return;
    }

    if (!drained.isEmpty()) {
      pour();
    } else if (stopPouring) {
      reset();
    } else {
      doTransfer(true);
    }
  }

  private boolean doTransfer(boolean execute) {
    IFluidHandler input = getInputHandler();
    IFluidHandler output = getOutputHandler();
    if (input != null && output != null) {
      FluidStack drained = input.drain(PACKET_SIZE, FluidAction.SIMULATE);
      if (!drained.isEmpty()) {
        int filled = output.fill(drained, FluidAction.SIMULATE);
        if (filled > 0) {
          drained.setAmount(MB_PER_TICK);
          if (filled <= MB_PER_TICK || output.fill(drained, FluidAction.SIMULATE) > 0) {
            if (execute) {
              this.drained = input.drain(filled, FluidAction.EXECUTE);
              if (faucetState == FaucetState.OFF || !renderFluid.isFluidEqual(this.drained)) {
                syncToClient(this.drained, true);
              }
              faucetState = FaucetState.POURING;
              pour();
            }
            return true;
          }
        }
      }

      if (lastRedstoneState) {
        if (execute && (faucetState == FaucetState.OFF || !renderFluid.isFluidEqual(FluidStack.EMPTY))) {
          syncToClient(FluidStack.EMPTY, true);
        }
        faucetState = FaucetState.POWERED;
        return false;
      }
    }
    if (execute) {
      reset();
    }
    return false;
  }

  private void pour() {
    if (drained.isEmpty()) {
      return;
    }

    IFluidHandler output = getOutputHandler();
    if (output != null) {
      FluidStack fillStack = drained.copy();
      fillStack.setAmount(Math.min(drained.getAmount(), MB_PER_TICK));

      int filled = output.fill(fillStack, IFluidHandler.FluidAction.SIMULATE);
      if (filled > 0) {
        if (!renderFluid.isFluidEqual(drained)) {
          syncToClient(drained, true);
        }

        this.drained.shrink(filled);
        fillStack.setAmount(filled);
        output.fill(fillStack, IFluidHandler.FluidAction.EXECUTE);
      }
    } else {
      reset();
    }
  }

  private void reset() {
    stopPouring = false;
    drained = FluidStack.EMPTY;
    if (faucetState != FaucetState.OFF || !renderFluid.isFluidEqual(drained)) {
      faucetState = FaucetState.OFF;
      syncToClient(FluidStack.EMPTY, false);
    }
  }

  public AABB getRenderBoundingBox() {
    return new AABB(worldPosition.getX(), worldPosition.getY() - 1, worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1);
  }


  /* NBT and networking */

  private void syncToClient(FluidStack fluid, boolean isPouring) {
    renderFluid = fluid.copy();
    if (level instanceof ServerLevel serverLevel) {
      TinkerNetwork.getInstance().sendToClientsAround(new FaucetActivationPacket(worldPosition, fluid, isPouring), serverLevel, getBlockPos());
    }
  }

  public void onActivationPacket(FluidStack fluid, boolean isPouring) {
    this.faucetState = isPouring ? FaucetState.POURING : FaucetState.OFF;
    this.renderFluid = fluid;
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(CompoundTag compound) {
    super.saveSynced(compound);
    compound.putByte(TAG_STATE, (byte)faucetState.ordinal());
    if (!renderFluid.isEmpty()) {
      compound.put(TAG_RENDER_FLUID, renderFluid.save(level.registryAccess(), new CompoundTag()));
    }
  }

  @Override
  public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    compound.putBoolean(TAG_STOP, stopPouring);
    compound.putBoolean(TAG_LAST_REDSTONE, lastRedstoneState);
    if (!drained.isEmpty()) {
      compound.put(TAG_DRAINED, drained.save(registries, new CompoundTag()));
    }
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);

    faucetState = FaucetState.fromIndex(compound.getByte(TAG_STATE));
    stopPouring = compound.getBoolean(TAG_STOP);
    lastRedstoneState = compound.getBoolean(TAG_LAST_REDSTONE);
    if (compound.contains(TAG_DRAINED, Tag.TAG_COMPOUND)) {
      drained = FluidStack.parseOptional(registries, compound.getCompound(TAG_DRAINED));
    } else {
      drained = FluidStack.EMPTY;
    }
    if (compound.contains(TAG_RENDER_FLUID, Tag.TAG_COMPOUND)) {
      renderFluid = FluidStack.parseOptional(registries, compound.getCompound(TAG_RENDER_FLUID));
    } else {
      renderFluid = FluidStack.EMPTY;
    }
  }

  private enum FaucetState {
    OFF,
    POURING,
    POWERED;

    public static FaucetState fromIndex(int index) {
      return switch (index) {
        case 1 -> POURING;
        case 2 -> POWERED;
        default -> OFF;
      };
    }
  }
}
