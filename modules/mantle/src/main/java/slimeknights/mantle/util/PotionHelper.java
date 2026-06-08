package slimeknights.mantle.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class PotionHelper {
  private PotionHelper() {}

  public static ItemStack setPotion(ItemStack stack, Potion potion) {
    return setPotion(stack, BuiltInRegistries.POTION.wrapAsHolder(potion));
  }

  public static ItemStack setPotion(ItemStack stack, Holder<Potion> potion) {
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
    return stack;
  }

  public static PotionContents getContents(ItemStack stack) {
    return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
  }

  public static PotionContents getContents(@Nullable CompoundTag tag) {
    if (tag == null || tag.isEmpty()) {
      return PotionContents.EMPTY;
    }
    if (!tag.contains("Potion", Tag.TAG_STRING)) {
      return PotionContents.EMPTY;
    }
    ResourceLocation id = ResourceLocation.tryParse(tag.getString("Potion"));
    if (id == null || !BuiltInRegistries.POTION.containsKey(id)) {
      return PotionContents.EMPTY;
    }
    return new PotionContents(BuiltInRegistries.POTION.getHolderOrThrow(ResourceKey.create(BuiltInRegistries.POTION.key(), id)));
  }

  @Nullable
  public static Potion getPotion(ItemStack stack) {
    return getContents(stack).potion().map(Holder::value).orElse(null);
  }

  @Nullable
  public static Potion getPotion(@Nullable CompoundTag tag) {
    return getContents(tag).potion().map(Holder::value).orElse(null);
  }

  public static boolean isPotion(ItemStack stack, Holder<Potion> potion) {
    return getContents(stack).is(potion);
  }

  public static boolean isPotion(ItemStack stack, Potion potion) {
    return isPotion(stack, BuiltInRegistries.POTION.wrapAsHolder(potion));
  }

  public static int getColor(ItemStack stack) {
    return getContents(stack).getColor();
  }

  public static int getColor(@Nullable CompoundTag tag) {
    return getContents(tag).getColor();
  }

  public static List<MobEffectInstance> getMobEffects(ItemStack stack) {
    List<MobEffectInstance> effects = new ArrayList<>();
    getContents(stack).getAllEffects().forEach(effects::add);
    return effects;
  }

  public static void addPotionTooltip(ItemStack stack, List<Component> tooltip, float durationFactor) {
    addPotionTooltip(getContents(stack), tooltip::add, durationFactor);
  }

  public static void addPotionTooltip(PotionContents contents, Consumer<Component> tooltip, float durationFactor) {
    contents.addPotionTooltip(tooltip, durationFactor, 20.0F);
  }

  public static CompoundTag toTag(PotionContents contents) {
    CompoundTag tag = new CompoundTag();
    contents.potion().ifPresent(potion -> tag.putString("Potion", potion.getRegisteredName()));
    contents.customColor().ifPresent(color -> tag.putInt("CustomPotionColor", color));
    return tag;
  }

  public static CompoundTag toTag(ItemStack stack) {
    return toTag(getContents(stack));
  }

  @Nullable
  public static CompoundTag getTag(FluidStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null ? data.copyTag() : null;
  }

  public static FluidStack withTag(FluidStack stack, @Nullable CompoundTag tag) {
    FluidStack copy = stack.copy();
    if (tag == null || tag.isEmpty()) {
      copy.remove(DataComponents.CUSTOM_DATA);
    } else {
      copy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    return copy;
  }

  public static FluidStack potionFluid(Fluid fluid, int amount, @Nullable CompoundTag tag) {
    return withTag(new FluidStack(fluid, amount), tag);
  }

  public static String getPotionName(Potion potion, String prefix) {
    return potion.getName(Optional.of(BuiltInRegistries.POTION.wrapAsHolder(potion)), prefix);
  }
}
