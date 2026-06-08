package slimeknights.tconstruct.plugin.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialRecipe;
import slimeknights.tconstruct.plugin.jei.material.MaterialsCraftingExtension;
import slimeknights.tconstruct.plugin.jei.material.ShapedMaterialsExtension;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Logic to show {@link ShapedMaterialRecipe} in JEI
 * @deprecated use {@link ShapedMaterialsExtension}
 */
@Deprecated
public class ShapedMaterialExtension implements ICraftingCategoryExtension {
  private final ShapedMaterialRecipe recipe;
  private final ItemStack plainResult;
  private final List<ItemStack> result;
  private final int[] materialSlots;
  public ShapedMaterialExtension(ShapedMaterialRecipe recipe) {
    this.recipe = recipe;
    MaterialValueIngredient materials = recipe.getMaterial();
    plainResult = recipe.getResultItem(Objects.requireNonNull(SafeClientAccess.getRegistryAccess()));
    if (materials != null) {
      this.result = MaterialRecipeCache.getAllRecipes().stream().filter(materials::test).flatMap(mat -> {
        ItemStack stack = plainResult.copy();
        recipe.setMaterial(stack, mat.getMaterial().getVariant());
        // add one copy of the stack per item in the nested ingredient, so the lengths match up
        return IntStream.range(0, mat.getIngredient().getItems().length).mapToObj(i -> stack);
      }).toList();
    } else {
      this.result = List.of(plainResult);
    }
    this.materialSlots = new int[0];
  }

  @Override
  public int getWidth() {
    return recipe.getWidth();
  }

  @Override
  public int getHeight() {
    return recipe.getHeight();
  }

  @Override
  public ResourceLocation getRegistryName() {
    return ResourceLocation.fromNamespaceAndPath("tconstruct", "jei/shaped_material/" + Integer.toHexString(System.identityHashCode(recipe)));
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focusGroup) {
    MaterialsCraftingExtension.setRecipe(this, builder, craftingGridHelper, recipe, result, plainResult, materialSlots);
  }
}
