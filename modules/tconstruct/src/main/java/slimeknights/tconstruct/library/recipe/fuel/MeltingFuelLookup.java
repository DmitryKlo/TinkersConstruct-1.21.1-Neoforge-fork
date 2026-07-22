package slimeknights.tconstruct.library.recipe.fuel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator.DuelSidedListener;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Class handling a recipe cache for fuel recipes, since any given entity type has one recipe
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MeltingFuelLookup {
  private static final ResourceLocation NETWORK_ID = Mantle.getResource("network");
  private static final ResourceLocation LAVA_ID = ResourceLocation.withDefaultNamespace("lava");
  private static final ResourceLocation BLAZING_BLOOD_ID = TConstruct.getResource("blazing_blood");
  /** Dummy fuel instance sine caches don't support caching null */
  private static final MeltingFuel EMPTY = new MeltingFuel(ResourceLocation.parse("missingno"), FluidIngredient.EMPTY, 0, 0, 0);
  /** Temperature for solid fuels in the heater */
  private static MeltingFuel SOLID = EMPTY;
  /** True once the client starts rebuilding this lookup from network-synced recipes. */
  private static boolean loadingNetworkRecipes = false;
  /** List of all recipes */
  private static final List<MeltingFuel> RECIPES = new ArrayList<>();
  /** Mapping from fluid to fuel */
  private static final Map<Fluid,MeltingFuel> CACHE = new HashMap<>();
  /** Logic to fill the cache */
  private static final Function<Fluid,MeltingFuel> LOOKUP = fluid -> {
    for (MeltingFuel recipe : RECIPES) {
      if (recipe.matches(fluid)) {
        return recipe;
      }
    }
    MeltingFuel fallback = fallbackFuel(fluid);
    if (fallback != null) {
      return fallback;
    }
    return EMPTY;
  };
  /** Listener to check when recipes reload */
  private static final DuelSidedListener LISTENER = RecipeCacheInvalidator.addDuelSidedListener(MeltingFuelLookup::clearLookup);

  /** Clears all fuel recipe caches and reload state. */
  private static void clearLookup() {
    SOLID = EMPTY;
    loadingNetworkRecipes = false;
    RECIPES.clear();
    CACHE.clear();
  }

  /**
   * Adds a melting fuel to the lookup
   * @param fuel   Fuel
   */
  public static void addFuel(MeltingFuel fuel) {
    // skip empty fuel
    if (fuel.getRate() == 0) {
      return;
    }
    LISTENER.checkClear();
    if (NETWORK_ID.equals(fuel.getId())) {
      if (!loadingNetworkRecipes) {
        clearLookup();
        loadingNetworkRecipes = true;
      }
    } else {
      loadingNetworkRecipes = false;
    }
    if (fuel.getInput() != FluidIngredient.EMPTY) {
      RECIPES.add(fuel);
    } else if (SOLID == EMPTY) {
      SOLID = fuel;
    } else {
      TConstruct.LOG.warn("Multiple fuel recipes for solid fuel. This usually indicates a datapack error and may cause desyncs. Original {}, latest {}", SOLID.getId(), fuel.getId());
    }
  }

  /** Checks if the given fluid is a fuel */
  public static boolean isFuel(Fluid fluid) {
    return getCachedFuel(fluid) != EMPTY;
  }

  /** Gets the properties for solid fuel */
  public static MeltingFuel getSolid() {
    if (SOLID == EMPTY) {
      return new MeltingFuel(TConstruct.getResource("smeltery/fuel/solid_fallback"), FluidIngredient.EMPTY, 0, 800, 8);
    }
    return SOLID;
  }

  /** Fallback while generated fuel recipes are not present in the ported runtime resources. */
  @Nullable
  private static MeltingFuel fallbackFuel(Fluid fluid) {
    ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
    if (fluid == Fluids.LAVA || LAVA_ID.equals(id)) {
      return new MeltingFuel(TConstruct.getResource("smeltery/fuel/lava_fallback"), FluidIngredient.of(Fluids.LAVA, 50), 100, 1000, 10);
    }
    if (BLAZING_BLOOD_ID.equals(id)) {
      return new MeltingFuel(TConstruct.getResource("smeltery/fuel/blazing_blood_fallback"), FluidIngredient.of(fluid, 50), 150, 1500, 15);
    }
    return null;
  }

  /**
   * Gets the recipe for the given fluid
   * @param fluid   Fluid found
   * @return  Recipe, or null if no recipe for this type
   */
  @Nullable
  public static MeltingFuel findFuel(Fluid fluid) {
    MeltingFuel recipe = getCachedFuel(fluid);
    if (recipe == EMPTY) {
      return null;
    }
    return recipe;
  }

  /** Gets a cached fuel recipe, computing outside the map mutation path to allow recipe reload invalidation during fallback creation. */
  private static MeltingFuel getCachedFuel(Fluid fluid) {
    MeltingFuel recipe = CACHE.get(fluid);
    if (recipe == null) {
      recipe = LOOKUP.apply(fluid);
      CACHE.put(fluid, recipe);
    }
    return recipe;
  }
}
