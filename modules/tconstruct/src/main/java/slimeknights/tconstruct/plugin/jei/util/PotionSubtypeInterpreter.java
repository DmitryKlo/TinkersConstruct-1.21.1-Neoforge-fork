package slimeknights.tconstruct.plugin.jei.util;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import slimeknights.mantle.util.PotionHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Common logic for subtype interpreter between the fluid and item form of our potion. Based on a JEI class with the same name */
public interface PotionSubtypeInterpreter<T> extends IIngredientSubtypeInterpreter<T> {
  @Nullable
  CompoundTag getTag(T ingredient);

  @Override
  default String apply(T ingredient, UidContext context) {
    CompoundTag tag = getTag(ingredient);
    if (tag == null || tag.isEmpty()) {
      return IIngredientSubtypeInterpreter.NONE;
    }
    PotionContents contents = PotionHelper.getContents(tag);
    if (contents == PotionContents.EMPTY) {
      return IIngredientSubtypeInterpreter.NONE;
    }
    String potionTypeString = contents.potion()
      .map(holder -> Potion.getName(java.util.Optional.of(holder), ""))
      .orElse("");
    StringBuilder stringBuilder = new StringBuilder(potionTypeString);
    List<MobEffectInstance> effects = new ArrayList<>();
    contents.getAllEffects().forEach(effects::add);
    for (MobEffectInstance effect : effects) {
      stringBuilder.append(";").append(effect);
    }
    return stringBuilder.toString();
  }
}
