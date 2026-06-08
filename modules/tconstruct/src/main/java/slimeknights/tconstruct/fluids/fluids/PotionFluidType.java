package slimeknights.tconstruct.fluids.fluids;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.fluid.texture.ClientTextureFluidType;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.util.PotionHelper;
import slimeknights.tconstruct.fluids.TinkerFluids;

import java.util.Objects;
import java.util.function.Consumer;

public class PotionFluidType extends FluidType {
  public PotionFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public String getDescriptionId(FluidStack stack) {
    Potion potion = PotionHelper.getPotion(PotionHelper.getTag(stack));
    if (potion == null) {
      return "item.minecraft.potion.effect.empty";
    }
    return PotionHelper.getPotionName(potion, "item.minecraft.potion.effect.");
  }

  @Override
  public ItemStack getBucket(FluidStack fluidStack) {
    ItemStack itemStack = new ItemStack(fluidStack.getFluid().getBucket());
    PotionContents contents = PotionHelper.getContents(PotionHelper.getTag(fluidStack));
    if (contents != PotionContents.EMPTY) {
      itemStack.set(DataComponents.POTION_CONTENTS, contents);
    }
    return itemStack;
  }

  @Override
  public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    consumer.accept(new ClientTextureFluidType(this) {
      @Override
      public int getTintColor(FluidStack stack) {
        CompoundTag tag = PotionHelper.getTag(stack);
        if (tag != null && tag.contains("CustomPotionColor", Tag.TAG_ANY_NUMERIC)) {
          return tag.getInt("CustomPotionColor") | 0xFF000000;
        }
        if (PotionHelper.getPotion(tag) == null) {
          return getTintColor();
        }
        return PotionHelper.getColor(tag) | 0xFF000000;
      }
    });
  }

  /** Creates the potion tag */
  private static CompoundTag potionTag(ResourceLocation location) {
    return PotionHelper.toTag(new PotionContents(BuiltInRegistries.POTION.getHolderOrThrow(ResourceKey.create(BuiltInRegistries.POTION.key(), location))));
  }

  /** Creates a fluid stack for the given potion */
  public static FluidStack potionFluid(ResourceKey<Potion> potion, int size) {
    CompoundTag tag = null;
    if (!potion.equals(Potions.WATER.unwrapKey().orElse(null))) {
      tag = potionTag(potion.location());
    }
    return PotionHelper.potionFluid(TinkerFluids.potion.get(), size, tag);
  }

  /** Creates a fluid stack for the given potion */
  @SuppressWarnings("deprecation")
  public static FluidStack potionFluid(Potion potion, int size) {
    CompoundTag tag = null;
    if (potion != Potions.WATER.value()) {
      tag = potionTag(BuiltInRegistries.POTION.getKey(potion));
    }
    return PotionHelper.potionFluid(TinkerFluids.potion.get(), size, tag);
  }

  /** Creates a fluid output for the given potion */
  @SuppressWarnings("deprecation")
  public static FluidOutput potionResult(Potion potion, int size) {
    CompoundTag tag = null;
    if (potion != Potions.WATER.value()) {
      tag = potionTag(BuiltInRegistries.POTION.getKey(potion));
    }
    return FluidOutput.fromTag(Objects.requireNonNull(TinkerFluids.potion.getCommonTag()), size, tag);
  }

  /** Creates a potion bucket for the given potion */
  public static ItemStack potionBucket(ResourceKey<Potion> potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    if (!potion.equals(Potions.WATER.unwrapKey().orElse(null))) {
      PotionHelper.setPotion(stack, BuiltInRegistries.POTION.getHolderOrThrow(potion));
    }
    return stack;
  }

  /** Creates a potion bucket for the given potion */
  @SuppressWarnings("deprecation")
  public static ItemStack potionBucket(Potion potion) {
    return PotionHelper.setPotion(new ItemStack(TinkerFluids.potion), potion);
  }
}
