package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.ingredient.compat.IIngredientSerializer;
import slimeknights.mantle.recipe.ingredient.compat.VanillaIngredientSerializer;
import slimeknights.mantle.util.CraftingHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.utils.JsonUtils;

import javax.annotation.Nullable;

/** Ingredient matching an item with no container item, used to ensure NBT fluid items are empty */
public class NoContainerIngredient extends NestedIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("no_container");

  protected NoContainerIngredient(Ingredient nested) {
    super(nested);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && super.test(stack) && !stack.hasCraftingRemainingItem();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public JsonElement toJson() {
    JsonElement nestedElement = serializeNested(nested);
    // if we are a vanilla ingredient, and not an array ingredient, serialize into the ingredient directly
    if (isVanilla(nested) && nestedElement.isJsonObject()) {
      JsonObject nestedObject = nestedElement.getAsJsonObject();
      nestedObject.addProperty("type", ID.toString());
      return nestedObject;
    }
    // if we have an array or a type, then serialize nested
    JsonObject json = JsonUtils.withType(ID);
    json.add("match", nestedElement);
    return json;
  }

  @Override
  public IIngredientSerializer<?> getSerializer() {
    return Serializer.INSTANCE;
  }

  public enum Serializer implements IIngredientSerializer<NoContainerIngredient> {
    INSTANCE;

    @Override
    public NoContainerIngredient parse(JsonObject json) {
      // if we have match, parse as a nested object. Without match, just parse the object as vanilla
      Ingredient ingredient;
      if (json.has("match")) {
        ingredient = CraftingHelper.getIngredient(json.get("match"), false);
      } else {
        JsonObject vanilla = json.deepCopy();
        vanilla.remove("type");
        ingredient = VanillaIngredientSerializer.INSTANCE.parse(vanilla);
      }
      return new NoContainerIngredient(ingredient);
    }

    @Override
    public NoContainerIngredient parse(FriendlyByteBuf buffer) {
      return new NoContainerIngredient(Ingredient.CONTENTS_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf) buffer));
    }

    @Override
    public void write(FriendlyByteBuf buffer, NoContainerIngredient ingredient) {
      Ingredient.CONTENTS_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf) buffer, ingredient.nested);
    }
  }


  /* Static constructors */

  /** Creates an instance from the given nested ingredient */
  public static Ingredient of(Ingredient ingredient) {
    return new NoContainerIngredient(ingredient).asIngredient();
  }

  /** Creates an instance from the given items */
  public static Ingredient of(ItemLike... items) {
    return of(Ingredient.of(items));
  }

  /** Creates an instance from the given stacks */
  public static Ingredient of(ItemStack... stacks) {
    return of(Ingredient.of(stacks));
  }

  /** Creates an instance from the given tag */
  public static Ingredient of(TagKey<Item> tag) {
    return of(Ingredient.of(tag));
  }
}
