package slimeknights.mantle.recipe.ingredient.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import slimeknights.mantle.util.CraftingHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Stream;

public abstract class AbstractIngredient {
  private final Value[] values;
  @Nullable
  private ItemStack[] items;

  protected AbstractIngredient(Stream<? extends Value> values) {
    this.values = values.toArray(Value[]::new);
  }

  public boolean test(@Nullable ItemStack stack) {
    if (stack == null) {
      return false;
    }
    for (ItemStack item : getItems()) {
      if (ItemStack.isSameItemSameComponents(stack, item)) {
        return true;
      }
    }
    return false;
  }

  public ItemStack[] getItems() {
    if (items == null) {
      items = Stream.of(values).flatMap(value -> value.getItems().stream()).toArray(ItemStack[]::new);
    }
    return items;
  }

  public IntList getStackingIds() {
    IntList ids = new IntArrayList();
    for (ItemStack stack : getItems()) {
      ids.add(BuiltInRegistries.ITEM.getId(stack.getItem()));
    }
    ids.sort(IntComparators.NATURAL_COMPARATOR);
    return ids;
  }

  public JsonElement toJson() {
    return JsonNull.INSTANCE;
  }

  protected void invalidate() {
    items = null;
  }

  public boolean isSimple() {
    return true;
  }

  public boolean isEmpty() {
    return values.length == 0;
  }

  public IIngredientSerializer<?> getSerializer() {
    return VanillaIngredientSerializer.INSTANCE;
  }

  public Ingredient asIngredient() {
    return CraftingHelper.asIngredient(this);
  }

  @Nullable
  public static AbstractIngredient unwrap(Ingredient ingredient) {
    ICustomIngredient custom = ingredient.getCustomIngredient();
    return custom instanceof CraftingHelper.LegacyCustomIngredient legacy ? legacy.ingredient() : null;
  }

  public interface Value {
    Collection<ItemStack> getItems();

    default JsonObject serialize() {
      return new JsonObject();
    }
  }

  public record ItemValue(ItemStack item) implements Value {
    @Override
    public Collection<ItemStack> getItems() {
      return java.util.List.of(item);
    }

    @Override
    public JsonObject serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("item", item.getItemHolder().unwrapKey().map(key -> key.location().toString()).orElse("minecraft:air"));
      return json;
    }
  }

  public static class TagValue implements Value {
    private final TagKey<Item> tag;

    public TagValue(TagKey<Item> tag) {
      this.tag = tag;
    }

    public TagKey<Item> tag() {
      return tag;
    }

    @Override
    public Collection<ItemStack> getItems() {
      return Ingredient.of(tag).getCustomIngredient() == null
        ? java.util.Arrays.asList(Ingredient.of(tag).getItems())
        : Ingredient.of(tag).getCustomIngredient().getItems().toList();
    }

    @Override
    public JsonObject serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("tag", tag.location().toString());
      return json;
    }
  }
}
