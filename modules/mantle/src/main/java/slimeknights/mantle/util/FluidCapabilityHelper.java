package slimeknights.mantle.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.Optional;

public final class FluidCapabilityHelper {
  private FluidCapabilityHelper() {}

  public static Optional<IFluidHandlerItem> item(ItemStack stack) {
    return Optional.ofNullable(stack.getCapability(Capabilities.FluidHandler.ITEM));
  }

  public static Optional<IFluidHandler> block(Level level, BlockEntity blockEntity, @Nullable Direction side) {
    BlockPos pos = blockEntity.getBlockPos();
    return Optional.ofNullable(level.getCapability(Capabilities.FluidHandler.BLOCK, pos, blockEntity.getBlockState(), blockEntity, side));
  }
}
