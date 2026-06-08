package slimeknights.tconstruct.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.PackOutput;
import slimeknights.mantle.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import slimeknights.mantle.recipe.data.IRecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.utils.ResourceId;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Shared logic for each module's recipe provider
 */
public abstract class BaseRecipeProvider extends RecipeProvider implements IConditionBuilder, IRecipeHelper {
  public BaseRecipeProvider(PackOutput generator, CompletableFuture<HolderLookup.Provider> registries) {
    super(generator, registries);
    TConstruct.sealTinkersClass(this, "BaseRecipeProvider", "BaseRecipeProvider is trivial to recreate and directly extending can lead to addon recipes polluting our namespace.");
  }

  @Override
  protected final void buildRecipes(RecipeOutput recipeOutput) {
    buildRecipes((Consumer<FinishedRecipe>) new RecipeOutputConsumer(recipeOutput));
  }

  protected abstract void buildRecipes(Consumer<FinishedRecipe> consumer);

  /** Casts the recipe consumer bridge back to a {@link RecipeOutput} for 1.21+ save overloads. */
  protected final RecipeOutputBridge asOutput(Consumer<FinishedRecipe> consumer) {
    return (RecipeOutputBridge) consumer;
  }

  /** Wraps typed TConstruct IDs in the given prefix and suffix. */
  protected final ResourceLocation wrap(ResourceId location, String prefix, String suffix) {
    return wrap(location.getLocation(), prefix, suffix);
  }

  /** Prefixes typed TConstruct IDs. */
  protected final ResourceLocation prefix(ResourceId location, String prefix) {
    return prefix(location.getLocation(), prefix);
  }

  /** Suffixes typed TConstruct IDs. */
  protected final ResourceLocation suffix(ResourceId location, String suffix) {
    return suffix(location.getLocation(), suffix);
  }

  @Override
  public String getModId() {
    return TConstruct.MOD_ID;
  }

  /** Bridges 1.20-style consumer APIs with 1.21+ RecipeOutput save methods. */
  protected interface RecipeOutputBridge extends Consumer<FinishedRecipe>, RecipeOutput {}

  private record RecipeOutputConsumer(RecipeOutput output) implements RecipeOutputBridge {
    @Override
    public void accept(FinishedRecipe finishedRecipe) {
      FinishedRecipe.consumer(output).accept(finishedRecipe);
    }

    @Override
    public void accept(ResourceLocation id, net.minecraft.world.item.crafting.Recipe<?> recipe, net.minecraft.advancements.AdvancementHolder advancement, net.neoforged.neoforge.common.conditions.ICondition... conditions) {
      output.accept(id, recipe, advancement, conditions);
    }

    @Override
    public Advancement.Builder advancement() {
      return output.advancement();
    }
  }
}
