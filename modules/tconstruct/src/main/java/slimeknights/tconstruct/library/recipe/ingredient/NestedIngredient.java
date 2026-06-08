package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.recipe.ingredient.compat.AbstractIngredient;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/** Ingredient that contains another ingredient nested inside */
public abstract class NestedIngredient extends AbstractIngredient {
  protected final Ingredient nested;

  protected NestedIngredient(Ingredient nested) {
    super(Stream.of());
    this.nested = nested;
  }

  /* Defer to nested */

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return nested.test(stack);
  }

  @Override
  public ItemStack[] getItems() {
    return nested.getItems();
  }

  @Override
  public IntList getStackingIds() {
    IntList ids = new IntArrayList();
    for (ItemStack stack : nested.getItems()) {
      ids.add(BuiltInRegistries.ITEM.getId(stack.getItem()));
    }
    ids.sort(IntComparators.NATURAL_COMPARATOR);
    return ids;
  }

  @Override
  public boolean isEmpty() {
    return nested.isEmpty();
  }

  @Override
  protected void invalidate() {
    super.invalidate();
  }

  @Override
  public boolean isSimple() {
    return nested.isSimple();
  }

  protected static JsonElement serializeNested(Ingredient nested) {
    return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, nested).result().orElse(JsonNull.INSTANCE);
  }

  protected static boolean isVanilla(Ingredient nested) {
    return !nested.isCustom();
  }
}
