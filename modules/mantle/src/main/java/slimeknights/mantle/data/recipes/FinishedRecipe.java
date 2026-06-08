package slimeknights.mantle.data.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface FinishedRecipe {
  static RecipeOutput output(Consumer<FinishedRecipe> consumer) {
    return new RecipeOutput() {
      private final Advancement.Builder advancement = Advancement.Builder.advancement();

      @Override
      public Advancement.Builder advancement() {
        return advancement;
      }

      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        @SuppressWarnings("unchecked")
        RecipeSerializer<Recipe<?>> serializer = (RecipeSerializer<Recipe<?>>) (RecipeSerializer<?>) recipe.getSerializer();
        JsonElement encoded = serializer.codec().codec().encodeStart(JsonOps.INSTANCE, recipe).getOrThrow(IllegalStateException::new);
        if (!encoded.isJsonObject()) {
          throw new IllegalStateException("Recipe codec for " + id + " did not encode to a JSON object");
        }
        JsonObject recipeData = encoded.getAsJsonObject().deepCopy();
        recipeData.remove("type");
        if (conditions.length > 0) {
          ICondition.writeConditions(JsonOps.INSTANCE, recipeData, Arrays.asList(conditions));
        }
        @Nullable
        final JsonObject advancementJson = advancement == null ? null : Advancement.CODEC.encodeStart(JsonOps.INSTANCE, advancement.value())
          .getOrThrow(IllegalStateException::new)
          .getAsJsonObject();
        @Nullable
        final ResourceLocation advancementId = advancement == null ? null : advancement.id();
        consumer.accept(new FinishedRecipe() {
          @Override
          public void serializeRecipeData(JsonObject json) {
            for (Map.Entry<String, JsonElement> entry : recipeData.entrySet()) {
              json.add(entry.getKey(), entry.getValue());
            }
          }

          @Override
          public ResourceLocation getId() {
            return id;
          }

          @Override
          public RecipeSerializer<?> getType() {
            return recipe.getSerializer();
          }

          @Nullable
          @Override
          public JsonObject serializeAdvancement() {
            return advancementJson;
          }

          @Nullable
          @Override
          public ResourceLocation getAdvancementId() {
            return advancementId;
          }
        });
      }
    };
  }

  /** Adapts legacy {@link Consumer} based recipe builders to a {@link RecipeOutput}. */
  static Consumer<FinishedRecipe> consumer(RecipeOutput output) {
    return finishedRecipe -> {
      JsonObject json = finishedRecipe.serializeRecipe().deepCopy();
      JsonElement typeElement = json.remove("type");
      if (typeElement == null) {
        throw new IllegalStateException("Recipe " + finishedRecipe.getId() + " missing type");
      }
      ResourceLocation typeId = ResourceLocation.parse(typeElement.getAsString());
      @SuppressWarnings("unchecked")
      RecipeSerializer<Recipe<?>> serializer = (RecipeSerializer<Recipe<?>>) (RecipeSerializer<?>) BuiltInRegistries.RECIPE_SERIALIZER.get(typeId);
      if (serializer == null) {
        throw new IllegalStateException("Unknown recipe serializer " + typeId);
      }
      ICondition[] conditions = extractConditions(json);
      Recipe<?> recipe = serializer.codec().codec().parse(JsonOps.INSTANCE, json).getOrThrow(IllegalStateException::new);
      AdvancementHolder advancement = null;
      ResourceLocation advancementId = finishedRecipe.getAdvancementId();
      JsonObject advancementJson = finishedRecipe.serializeAdvancement();
      if (advancementId != null && advancementJson != null) {
        Advancement advancementValue = Advancement.CODEC.parse(JsonOps.INSTANCE, advancementJson).getOrThrow(IllegalStateException::new);
        advancement = new AdvancementHolder(advancementId, advancementValue);
      }
      output.accept(finishedRecipe.getId(), recipe, advancement, conditions);
    };
  }

  private static ICondition[] extractConditions(JsonObject json) {
    if (json.has(ConditionalOps.DEFAULT_CONDITIONS_KEY)) {
      List<ICondition> conditions = ICondition.LIST_CODEC.parse(JsonOps.INSTANCE, json.remove(ConditionalOps.DEFAULT_CONDITIONS_KEY))
        .getOrThrow(IllegalStateException::new);
      return conditions.toArray(ICondition[]::new);
    }
    if (json.has("conditions")) {
      JsonArray array = json.getAsJsonArray("conditions");
      json.remove("conditions");
      List<ICondition> conditions = new ArrayList<>(array.size());
      for (JsonElement element : array) {
        conditions.add(ICondition.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow(IllegalStateException::new));
      }
      return conditions.toArray(ICondition[]::new);
    }
    return new ICondition[0];
  }

  default JsonObject serializeRecipe() {
    JsonObject json = new JsonObject();
    serializeRecipeData(json);
    return json;
  }

  void serializeRecipeData(JsonObject json);

  ResourceLocation getId();

  RecipeSerializer<?> getType();

  @Nullable
  JsonObject serializeAdvancement();

  @Nullable
  ResourceLocation getAdvancementId();
}
