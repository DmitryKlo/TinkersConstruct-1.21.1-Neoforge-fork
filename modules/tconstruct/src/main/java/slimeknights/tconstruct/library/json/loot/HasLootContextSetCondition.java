package slimeknights.tconstruct.library.json.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.shared.TinkerCommons;

/**
 * Loot condition that only runs if all required values in the given loot context set are present. Good heuristic for using that set.
 * TODO: migrate to Mantle
 */
public record HasLootContextSetCondition(LootContextParamSet set) implements LootItemCondition {
  /** Creates a new builder instance */
  public static Builder builder(LootContextParamSet set) {
    return new Builder(set);
  }

  @Override
  public LootItemConditionType getType() {
    return TinkerCommons.hasLootContextSet.get();
  }

  @Override
  public boolean test(LootContext context) {
    for (LootContextParam<?> param : set.getRequired()) {
      if (!context.hasParam(param)) {
        return false;
      }
    }
    return true;
  }

  /** Builder logic for this condition */
  public record Builder(LootContextParamSet set) implements LootItemCondition.Builder {
    @Override
    public LootItemCondition build() {
      return new HasLootContextSetCondition(set);
    }
  }

  /** Serializer logic */
  public static class Serializer implements slimeknights.mantle.loot.legacy.LegacyLootSerializer<HasLootContextSetCondition> {
    private static ResourceLocation getKey(LootContextParamSet set) {
      String name =
        set == LootContextParamSets.CHEST ? "chest" :
        set == LootContextParamSets.COMMAND ? "command" :
        set == LootContextParamSets.SELECTOR ? "selector" :
        set == LootContextParamSets.FISHING ? "fishing" :
        set == LootContextParamSets.ENTITY ? "entity" :
        set == LootContextParamSets.EQUIPMENT ? "equipment" :
        set == LootContextParamSets.ARCHAEOLOGY ? "archaeology" :
        set == LootContextParamSets.GIFT ? "gift" :
        set == LootContextParamSets.PIGLIN_BARTER ? "piglin_barter" :
        set == LootContextParamSets.VAULT ? "vault" :
        set == LootContextParamSets.BLOCK ? "block" :
        set == LootContextParamSets.SHEARING ? "shearing" :
        "empty";
      return ResourceLocation.withDefaultNamespace(name);
    }

    private static LootContextParamSet getSet(ResourceLocation key) {
      return switch (key.getPath()) {
        case "chest" -> LootContextParamSets.CHEST;
        case "command" -> LootContextParamSets.COMMAND;
        case "selector" -> LootContextParamSets.SELECTOR;
        case "fishing" -> LootContextParamSets.FISHING;
        case "entity" -> LootContextParamSets.ENTITY;
        case "equipment" -> LootContextParamSets.EQUIPMENT;
        case "archaeology" -> LootContextParamSets.ARCHAEOLOGY;
        case "gift" -> LootContextParamSets.GIFT;
        case "piglin_barter" -> LootContextParamSets.PIGLIN_BARTER;
        case "vault" -> LootContextParamSets.VAULT;
        case "block" -> LootContextParamSets.BLOCK;
        case "shearing" -> LootContextParamSets.SHEARING;
        default -> LootContextParamSets.EMPTY;
      };
    }

    @Override
    public void serialize(JsonObject json, HasLootContextSetCondition value, JsonSerializationContext context) {
      json.addProperty("set", getKey(value.set).toString());
    }

    @Override
    public HasLootContextSetCondition deserialize(JsonObject json, JsonDeserializationContext context) {
      ResourceLocation key = JsonHelper.getResourceLocation(json, "set");
      return new HasLootContextSetCondition(getSet(key));
    }
  }
}
