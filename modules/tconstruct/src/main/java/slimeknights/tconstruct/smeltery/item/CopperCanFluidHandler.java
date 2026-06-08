package slimeknights.tconstruct.smeltery.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.tconstruct.library.recipe.FluidValues;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Fluid handler instance for the copper can item */
@AllArgsConstructor
public class CopperCanFluidHandler implements IFluidHandlerItem {
  @Getter
  private final ItemStack container;

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return true;
  }

  private int getCapacity() {
    return FluidValues.INGOT * container.getCount();
  }

  @Override
  public int getTankCapacity(int tank) {
    return getCapacity();
  }

  private Fluid getFluid() {
    return CopperCanItem.getFluid(container);
  }

  @Nullable
  private CompoundTag getFluidTag() {
    return CopperCanItem.getFluidTag(container);
  }

  @Nonnull
  @Override
  public FluidStack getFluidInTank(int tank) {
    Fluid fluid = getFluid();
    if (fluid == Fluids.EMPTY) {
      return FluidStack.EMPTY;
    }
    return new FluidStack(getFluid(), getCapacity());
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    int capacity = getCapacity();
    if (getFluid() != Fluids.EMPTY || resource.getAmount() < capacity) {
      return 0;
    }
    if (action.execute()) {
      CopperCanItem.setFluid(container, resource);
    }
    return capacity;
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    int capacity = getCapacity();
    if (resource.isEmpty() || resource.getAmount() < capacity) {
      return FluidStack.EMPTY;
    }
    Fluid fluid = getFluid();
    if (fluid == Fluids.EMPTY || fluid != resource.getFluid()) {
      return FluidStack.EMPTY;
    }
    FluidStack output = new FluidStack(fluid, capacity);
    if (!FluidStack.isSameFluidSameComponents(resource, output)) {
      return FluidStack.EMPTY;
    }
    if (action.execute()) {
      CopperCanItem.setFluid(container, FluidStack.EMPTY);
    }
    return output;
  }

  @Nonnull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    int capacity = getCapacity();
    if (maxDrain < capacity) {
      return FluidStack.EMPTY;
    }
    Fluid fluid = getFluid();
    if (fluid == Fluids.EMPTY) {
      return FluidStack.EMPTY;
    }
    FluidStack output = new FluidStack(fluid, capacity);
    if (action.execute()) {
      CopperCanItem.setFluid(container, FluidStack.EMPTY);
    }
    return output;
  }
}
