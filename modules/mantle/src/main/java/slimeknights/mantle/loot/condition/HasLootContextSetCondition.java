package slimeknights.mantle.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.util.JsonHelper;

/** Loot condition that only runs if all required values in the given loot context set are present. Good heuristic for using that set. */
public record HasLootContextSetCondition(LootContextParamSet set) implements LootItemCondition {
  public static final MapCodec<HasLootContextSetCondition> CODEC = MapCodec.unit(() -> new HasLootContextSetCondition(LootContextParamSets.EMPTY));
  /** Creates a new builder instance */
  public static Builder builder(LootContextParamSet set) {
    return new Builder(set);
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.HAS_CONTEXT_SET;
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
    @Override
    public void serialize(JsonObject json, HasLootContextSetCondition value, JsonSerializationContext context) {
      json.add("set", JsonHelper.serialize(LootContextParamSets.CODEC, value.set));
    }

    @Override
    public HasLootContextSetCondition deserialize(JsonObject json, JsonDeserializationContext context) {
      LootContextParamSet set = JsonHelper.parse(LootContextParamSets.CODEC, json.get("set"));
      return new HasLootContextSetCondition(set);
    }
  }
}
