package slimeknights.mantle.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** Fluid where up is down and down is up */
public abstract class InvertedFluid extends BaseFlowingFluid {
  protected InvertedFluid(Properties properties) {
    super(properties);
  }

  protected boolean affectsFlow(FluidState state) {
    return state.getType().isSame(this);
  }

  @Override
  public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState fluid) {
    double xHeight = 0.0D;
    double zHeight = 0.0D;
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    for (Direction direction : Direction.Plane.HORIZONTAL) {
      mutable.setWithOffset(pos, direction);
      FluidState sideFluid = level.getFluidState(mutable);
      if (this.affectsFlow(sideFluid)) {
        float sideHeight = sideFluid.getOwnHeight();
        float deltaHeight = 0.0F;
        if (sideHeight == 0.0F) {
          if (!level.getBlockState(mutable).blocksMotion()) {
            BlockPos above = mutable.above();
            FluidState aboveFluid = level.getFluidState(above);
            if (this.affectsFlow(aboveFluid)) {
              sideHeight = aboveFluid.getOwnHeight();
              if (sideHeight > 0.0F) {
                deltaHeight = fluid.getOwnHeight() - sideHeight + 0.8888889F;
              }
            }
          }
        } else if (sideHeight > 0.0F) {
          deltaHeight = fluid.getOwnHeight() - sideHeight;
        }

        if (deltaHeight != 0.0F) {
          xHeight += direction.getStepX() * deltaHeight;
          zHeight += direction.getStepZ() * deltaHeight;
        }
      }
    }

    Vec3 vector = new Vec3(xHeight, 0.0D, zHeight);
    if (fluid.getValue(FALLING)) {
      for (Direction direction : Direction.Plane.HORIZONTAL) {
        mutable.setWithOffset(pos, direction);
        if (this.isSolidFace(level, mutable, direction) || this.isSolidFace(level, mutable.below(), direction)) {
          vector = vector.normalize().add(0.0D, 6.0D, 0.0D);
          break;
        }
      }
    }

    return vector.normalize();
  }

  @Override
  protected boolean isSolidFace(BlockGetter level, BlockPos neighbor, Direction side) {
    BlockState block = level.getBlockState(neighbor);
    FluidState fluid = level.getFluidState(neighbor);
    return !fluid.getType().isSame(this) && (side == Direction.DOWN || !(block.getBlock() instanceof IceBlock) && block.isFaceSturdy(level, neighbor, side));
  }

  public static class Flowing extends InvertedFluid {
    public Flowing(Properties properties) {
      super(properties);
      this.registerDefaultState(this.getStateDefinition().any().setValue(LEVEL, 7));
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
      super.createFluidStateDefinition(builder);
      builder.add(LEVEL);
    }

    @Override
    public int getAmount(FluidState state) {
      return state.getValue(LEVEL);
    }

    @Override
    public boolean isSource(FluidState state) {
      return false;
    }
  }

  public static class Source extends InvertedFluid {
    public Source(Properties properties) {
      super(properties);
    }

    @Override
    public int getAmount(FluidState state) {
      return 8;
    }

    @Override
    public boolean isSource(FluidState state) {
      return true;
    }
  }
}
