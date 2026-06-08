package slimeknights.tconstruct.fluids.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

/** Recipe for transforming a bottle, depending on a vanilla brewing recipe to get the ingredient */
public class BottleBrewingRecipe extends BrewingRecipe {
  private static final Ingredient SPLASH_INGREDIENT = Ingredient.of(Items.GUNPOWDER);
  private static final Ingredient LINGERING_INGREDIENT = Ingredient.of(Items.DRAGON_BREATH);

  private final Item from;
  private final Item to;
  public BottleBrewingRecipe(Ingredient input, Item from, Item to, ItemStack output) {
    super(input, Ingredient.EMPTY, output);
    this.from = from;
    this.to = to;
  }

  @Override
  public boolean isIngredient(ItemStack stack) {
    if (from == Items.POTION && to == Items.SPLASH_POTION) {
      return SPLASH_INGREDIENT.test(stack);
    }
    if (from == Items.SPLASH_POTION && to == Items.LINGERING_POTION) {
      return LINGERING_INGREDIENT.test(stack);
    }
    return false;
  }

  @Override
  public Ingredient getIngredient() {
    if (from == Items.POTION && to == Items.SPLASH_POTION) {
      return SPLASH_INGREDIENT;
    }
    if (from == Items.SPLASH_POTION && to == Items.LINGERING_POTION) {
      return LINGERING_INGREDIENT;
    }
    return Ingredient.EMPTY;
  }
}
