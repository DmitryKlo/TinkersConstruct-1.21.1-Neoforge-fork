package slimeknights.mantle.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.data.JsonCodec;
import slimeknights.mantle.recipe.ingredient.compat.AbstractIngredient;
import slimeknights.mantle.recipe.ingredient.compat.IIngredientSerializer;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class CraftingHelper {
  private CraftingHelper() {}

  private static final Map<Object, IngredientType<?>> INGREDIENT_TYPES_BY_SERIALIZER = new IdentityHashMap<>();

  public static void register(Object serializer) {}

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void register(ResourceLocation id, Object serializer) {
    if (serializer instanceof IIngredientSerializer ingredientSerializer) {
      Optional<IngredientType<?>> existing = NeoForgeRegistries.INGREDIENT_TYPES.getOptional(id);
      if (existing.isPresent()) {
        INGREDIENT_TYPES_BY_SERIALIZER.put(serializer, existing.get());
        return;
      }

      final IngredientType<LegacyCustomIngredient>[] holder = new IngredientType[1];
      IngredientType<LegacyCustomIngredient> type = new IngredientType<>(
        MapCodec.assumeMapUnsafe(new JsonCodec<>() {
          @Override
          public LegacyCustomIngredient deserialize(JsonElement element, DynamicOps<?> ops) {
            return new LegacyCustomIngredient(asAbstractIngredient(ingredientSerializer.parse(GsonHelper.convertToJsonObject(element, "ingredient")), id), holder[0]);
          }

          @Override
          public JsonElement serialize(LegacyCustomIngredient object, DynamicOps<?> ops) {
            return object.ingredient().toJson();
          }
        }),
        StreamCodec.of(
          (buffer, ingredient) -> ingredientSerializer.write(buffer, ingredient.ingredient()),
          buffer -> new LegacyCustomIngredient(asAbstractIngredient(ingredientSerializer.parse(buffer), id), holder[0])));
      holder[0] = type;
      Registry.register(NeoForgeRegistries.INGREDIENT_TYPES, id, type);
      INGREDIENT_TYPES_BY_SERIALIZER.put(serializer, type);
    }
  }

  private static AbstractIngredient asAbstractIngredient(Object parsed, ResourceLocation id) {
    if (parsed instanceof AbstractIngredient ingredient) {
      return ingredient;
    }
    throw new IllegalArgumentException("Ingredient serializer " + id + " returned " + parsed + " instead of an AbstractIngredient");
  }

  public static Ingredient asIngredient(AbstractIngredient ingredient) {
    IngredientType<?> type = INGREDIENT_TYPES_BY_SERIALIZER.get(ingredient.getSerializer());
    if (type == null) {
      throw new IllegalStateException("Missing NeoForge ingredient type for " + ingredient.getSerializer());
    }
    return new Ingredient(new LegacyCustomIngredient(ingredient, type));
  }

  public static ResourceLocation getID(Object serializer) {
    return ResourceLocation.withDefaultNamespace("empty");
  }

  public static JsonObject serialize(ICondition condition) {
    JsonObject json = new JsonObject();
    json.addProperty("type", condition.codec().toString());
    return json;
  }

  public static JsonArray serialize(ICondition[] conditions) {
    JsonArray array = new JsonArray();
    for (ICondition condition : conditions) {
      array.add(serialize(condition));
    }
    return array;
  }

  public static JsonArray serialize(Iterable<ICondition> conditions) {
    JsonArray array = new JsonArray();
    for (ICondition condition : conditions) {
      array.add(serialize(condition));
    }
    return array;
  }

  public static boolean processConditions(JsonObject json, String key, Object context) {
    return !json.has(key) || processConditions(GsonHelper.getAsJsonArray(json, key), context);
  }

  public static boolean processConditions(JsonArray json, Object context) {
    for (JsonElement element : json) {
      if (!processCondition(element.getAsJsonObject())) {
        return false;
      }
    }
    return true;
  }

  private static boolean processCondition(JsonObject json) {
    String type = GsonHelper.getAsString(json, "type", "neoforge:true").replace("forge:", "neoforge:");
    return switch (type) {
      case "neoforge:true" -> true;
      case "neoforge:false" -> false;
      case "neoforge:not" -> !processCondition(GsonHelper.getAsJsonObject(json, "value"));
      case "neoforge:and" -> processConditions(GsonHelper.getAsJsonArray(json, "values"), null);
      case "neoforge:or" -> {
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "values")) {
          if (processCondition(element.getAsJsonObject())) {
            yield true;
          }
        }
        yield false;
      }
      case "neoforge:mod_loaded" -> ModList.get().isLoaded(GsonHelper.getAsString(json, "modid"));
      case "neoforge:item_exists" -> BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(GsonHelper.getAsString(json, "item"))).isPresent();
      default -> true;
    };
  }

  public static ICondition getCondition(JsonObject json) {
    return FalseCondition.INSTANCE;
  }

  public static Ingredient getIngredient(JsonElement element, boolean allowEmpty) {
    Ingredient ingredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, element).result().orElse(Ingredient.EMPTY);
    if (!allowEmpty && ingredient.isEmpty()) {
      throw new IllegalArgumentException("Ingredient cannot be empty");
    }
    return ingredient;
  }

  public static JsonElement serializeIngredient(Ingredient ingredient) {
    return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).result().orElseGet(JsonObject::new);
  }

  public static ItemStack getItemStack(JsonObject json, boolean readNbt) {
    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(GsonHelper.getAsString(json, "item")));
    int count = GsonHelper.getAsInt(json, "count", 1);
    return new ItemStack(item, count);
  }

  public record LegacyCustomIngredient(AbstractIngredient ingredient, IngredientType<?> type) implements ICustomIngredient {
    @Override
    public boolean test(ItemStack stack) {
      return ingredient.test(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
      return Arrays.stream(ingredient.getItems());
    }

    @Override
    public boolean isSimple() {
      return ingredient.isSimple();
    }

    @Override
    public IngredientType<?> getType() {
      return type;
    }
  }
}
