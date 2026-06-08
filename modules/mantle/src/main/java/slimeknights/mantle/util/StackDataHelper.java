package slimeknights.mantle.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

public final class StackDataHelper {
  private StackDataHelper() {}

  @Nullable
  public static CompoundTag getTag(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null ? null : data.copyTag();
  }

  public static CompoundTag getOrCreateTag(ItemStack stack) {
    CompoundTag tag = getTag(stack);
    return tag == null ? new CompoundTag() : tag;
  }

  public static boolean hasTag(ItemStack stack) {
    CompoundTag tag = getTag(stack);
    return tag != null && !tag.isEmpty();
  }

  public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
    if (tag == null || tag.isEmpty()) {
      stack.remove(DataComponents.CUSTOM_DATA);
    } else {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
  }
}
