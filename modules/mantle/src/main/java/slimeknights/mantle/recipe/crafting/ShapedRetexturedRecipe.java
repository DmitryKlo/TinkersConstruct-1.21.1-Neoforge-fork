package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.util.CraftingHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.recipe.helper.LoggingRecipeSerializer;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

/** Recipe which sets the texture for a {@link slimeknights.mantle.block.RetexturedBlock} based on an ingredient input. */
// TODO 1.21: rework to be more like the ShapedMaterialsRecipe from Tinkers for more efficient network syncing
@SuppressWarnings("WeakerAccess")
public class ShapedRetexturedRecipe extends ShapedRecipe {
  /** Ingredient used to determine the texture on the output */
  @Getter
  private final Ingredient texture;
  @Getter
  private final ResourceLocation id;
  private final boolean matchAll;

  /** Creates a new recipe using the passed parameters */
  protected ShapedRetexturedRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result, boolean showNotification, Ingredient texture, boolean matchAll) {
    super(group, category, new ShapedRecipePattern(width, height, ingredients, Optional.empty()), result, showNotification);
    this.id = id;
    this.texture = texture;
    this.matchAll = matchAll;
  }

  /**
   * Creates a new recipe using an existing shaped recipe
   * @param orig       Shaped recipe to copy
   * @param texture    Ingredient to use for the texture
   * @param matchAll   If true, all inputs must match for the recipe to match
   */
  protected ShapedRetexturedRecipe(ResourceLocation id, ShapedRecipe orig, Ingredient texture, boolean matchAll) {
    this(id, orig.getGroup(), orig.category(), orig.getWidth(), orig.getHeight(), orig.getIngredients(), orig.getResultItem(null), orig.showNotification(), texture, matchAll);
  }

  /**
   * Gets the output using the given texture
   * @param texture  Texture to use
   * @return  Output with texture. Will be blank if the input is not a block
   */
  public ItemStack getResultItem(Item texture, HolderLookup.Provider access) {
    return RetexturedHelper.setTexture(getResultItem(access).copy(), Block.byItem(texture));
  }

  @Override
  public ItemStack assemble(CraftingInput craftMatrix, HolderLookup.Provider access) {
    ItemStack result = super.assemble(craftMatrix, access);
    Block currentTexture = null;
    for (int i = 0; i < craftMatrix.size(); i++) {
      ItemStack stack = craftMatrix.getItem(i);
      if (!stack.isEmpty() && texture.test(stack)) {
        // fetch texture from the block if it has one
        Block block = RetexturedHelper.getTexture(stack);
        // assuming it does not, use the block itself as the texture (provided it is not the result that is)
        if (block == Blocks.AIR && stack.getItem() != result.getItem()) {
          block = Block.byItem(stack.getItem());
        }
        // if no texture, skip
        if (block == Blocks.AIR) {
          continue;
        }

        // if we have not found a texture yet, store the found block
        if (currentTexture == null) {
          currentTexture = block;
          // match all means we must check the rest. If not match all, we can be done
          if (!matchAll) {
            break;
          }

          // if we found a texture before, must match or we do no texture
        } else if (currentTexture != block) {
          currentTexture = null;
          break;
        }
      }
    }

    // set the texture if found. No texture will use the fallback
    if (currentTexture != null) {
      return RetexturedHelper.setTexture(result, currentTexture);
    }
    return result;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.CRAFTING_SHAPED_RETEXTURED.get();
  }

  public static class Serializer implements LoggingRecipeSerializer<ShapedRetexturedRecipe> {
    private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(s -> {
      if (s.length() != 1) {
        return DataResult.error(() -> "Invalid key entry: '" + s + "' is an invalid symbol (must be 1 character only).");
      }
      return " ".equals(s)
        ? DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.")
        : DataResult.success(s.charAt(0));
    }, String::valueOf);

    /** Resolves texture from a key symbol or a full ingredient definition in the same recipe object */
    private static final MapCodec<Ingredient> TEXTURE_CODEC = new MapCodec<>() {
      @Override
      public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(ops.createString("texture"));
      }

      @Override
      public <T> DataResult<Ingredient> decode(DynamicOps<T> ops, MapLike<T> input) {
        T textureValue = input.get("texture");
        if (textureValue == null) {
          return DataResult.error(() -> "Missing texture");
        }
        DataResult<String> symbol = ops.getStringValue(textureValue);
        if (symbol.result().isPresent()) {
          String textureKey = symbol.getOrThrow();
          if (textureKey.length() != 1) {
            return DataResult.error(() -> "Invalid texture key: '" + textureKey + "' is an invalid symbol (must be 1 character only).");
          }
          T keyValue = input.get("key");
          if (keyValue == null) {
            return DataResult.error(() -> "Missing key");
          }
          return ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, Ingredient.CODEC_NONEMPTY)
            .parse(ops, keyValue)
            .flatMap(key -> {
              Ingredient ingredient = key.get(textureKey.charAt(0));
              if (ingredient == null || ingredient.isEmpty()) {
                return DataResult.error(() -> "Texture ingredient references symbol '" + textureKey + "' but it's not defined in the key");
              }
              return DataResult.success(ingredient);
            });
        }
        JsonElement element = ops.convertTo(JsonOps.INSTANCE, textureValue);
        try {
          Ingredient parsed = CraftingHelper.getIngredient(element, false);
          Mantle.logger.warn("Using deprecated ingredient format on 'texture' for `mantle:crafting_shaped_retextured`. Use key instead.");
          return DataResult.success(parsed);
        } catch (IllegalArgumentException e) {
          return DataResult.error(e::getMessage);
        }
      }

      @Override
      public <T> RecordBuilder<T> encode(Ingredient texture, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        return prefix.add("texture", texture, Ingredient.CODEC);
      }
    };

    public static final MapCodec<ShapedRetexturedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      ShapedRecipe.Serializer.CODEC.forGetter(recipe -> recipe),
      TEXTURE_CODEC.forGetter(recipe -> recipe.texture),
      Codec.BOOL.optionalFieldOf("match_all", Boolean.FALSE).forGetter(recipe -> recipe.matchAll)
    ).apply(instance, (shaped, texture, matchAll) -> new ShapedRetexturedRecipe(Mantle.getResource("codec"), shaped, texture, matchAll)));

    @Nullable
    @Override
    public ShapedRetexturedRecipe fromNetworkSafe(ResourceLocation recipeId, FriendlyByteBuf buffer) {
      ShapedRecipe recipe = SHAPED_RECIPE.streamCodec().decode((RegistryFriendlyByteBuf) buffer);
      return new ShapedRetexturedRecipe(recipeId, recipe, Ingredient.CONTENTS_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer), buffer.readBoolean());
    }

    @Override
    public void toNetworkSafe(FriendlyByteBuf buffer, ShapedRetexturedRecipe recipe) {
      SHAPED_RECIPE.streamCodec().encode((RegistryFriendlyByteBuf) buffer, recipe);
      Ingredient.CONTENTS_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, recipe.texture);
      buffer.writeBoolean(recipe.matchAll);
    }

    @Override
    public MapCodec<ShapedRetexturedRecipe> codec() {
      return CODEC;
    }
  }
}
