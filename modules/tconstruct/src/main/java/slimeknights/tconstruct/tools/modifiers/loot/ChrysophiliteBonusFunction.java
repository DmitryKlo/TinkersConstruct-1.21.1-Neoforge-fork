package slimeknights.tconstruct.tools.modifiers.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.BinomialWithBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.OreDrops;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.UniformBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.modifiers.traits.skull.ChrysophiliteModifier;

import java.util.List;
import java.util.Set;

/** Loot modifier to boost drops based on teh chrysophilite amount */
public class ChrysophiliteBonusFunction extends LootItemConditionalFunction {
  public static final MapCodec<ChrysophiliteBonusFunction> CODEC = MapCodec.unit(() -> new ChrysophiliteBonusFunction(List.of(), new OreDrops(), true));
  public static final Serializer SERIALIZER = new Serializer();

  /** Formula to apply */
  private final Formula formula;
  /** If true, the includes the helmet in the level, if false level is just gold pieces */
  private final boolean includeBase;
  protected ChrysophiliteBonusFunction(List<LootItemCondition> conditions, Formula formula, boolean includeBase) {
    super(conditions);
    this.formula = formula;
    this.includeBase = includeBase;
  }

  /** Creates a generic builder */
  public static Builder<?> builder(Formula formula, boolean includeBase) {
    return simpleBuilder(conditions -> new ChrysophiliteBonusFunction(conditions, formula, includeBase));
  }

  /** Creates a builder for the binomial with bonus formula */
  public static Builder<?> binomialWithBonusCount(float probability, int extra, boolean includeBase) {
    return builder(new BinomialWithBonusCount(extra, probability), includeBase);
  }

  /** Creates a builder for the ore drops formula */
  public static Builder<?> oreDrops(boolean includeBase) {
    return builder(new OreDrops(), includeBase);
  }

  /** Creates a builder for the uniform bonus count */
  public static Builder<?> uniformBonusCount(int bonusMultiplier, boolean includeBase) {
    return builder(new UniformBonusCount(bonusMultiplier), includeBase);
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    int level = ChrysophiliteModifier.getTotalGold(context.getParamOrNull(LootContextParams.THIS_ENTITY));
    if (!includeBase) {
      level--;
    }
    if (level > 0) {
      stack.setCount(formula.calculateNewCount(context.getRandom(), stack.getCount(), level));
    }
    return stack;
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(LootContextParams.THIS_ENTITY);
  }

  @Override
  public LootItemFunctionType<ChrysophiliteBonusFunction> getType() {
    return TinkerModifiers.chrysophiliteBonusFunction.get();
  }

  private static Formula parseFormula(ResourceLocation id, JsonObject parameters, JsonDeserializationContext context) {
    if (id.equals(BinomialWithBonusCount.TYPE)) {
      return new BinomialWithBonusCount(GsonHelper.getAsInt(parameters, "extra", 0), GsonHelper.getAsFloat(parameters, "probability", 0.5f));
    }
    if (id.equals(UniformBonusCount.TYPE)) {
      return new UniformBonusCount(GsonHelper.getAsInt(parameters, "bonusMultiplier", 1));
    }
    if (id.equals(OreDrops.TYPE)) {
      return new OreDrops();
    }
    throw new JsonParseException("Invalid formula id: " + id);
  }

  /** Serializer class */
  public static class Serializer implements slimeknights.mantle.loot.legacy.LegacyLootSerializer<ChrysophiliteBonusFunction> {
    @Override
    public void serialize(JsonObject json, ChrysophiliteBonusFunction loot, JsonSerializationContext context) {
      json.addProperty("formula", formulaId(loot.formula).toString());
      JsonObject parameters = new JsonObject();
      serializeFormulaParams(loot.formula, parameters);
      if (!parameters.isEmpty()) {
        json.add("parameters", parameters);
      }
      json.addProperty("include_base", loot.includeBase);
    }

    @Override
    public ChrysophiliteBonusFunction deserialize(JsonObject json, JsonDeserializationContext context) {
      ResourceLocation id = JsonHelper.getResourceLocation(json, "formula");
      JsonObject parameters = json.has("parameters") ? GsonHelper.getAsJsonObject(json, "parameters") : new JsonObject();
      Formula formula = parseFormula(id, parameters, context);
      boolean includeBase = GsonHelper.getAsBoolean(json, "include_base", true);
      return new ChrysophiliteBonusFunction(List.of(), formula, includeBase);
    }
  }

  private static ResourceLocation formulaId(Formula formula) {
    if (formula instanceof BinomialWithBonusCount) {
      return ResourceLocation.withDefaultNamespace("binomial_with_bonus_count");
    }
    if (formula instanceof UniformBonusCount) {
      return ResourceLocation.withDefaultNamespace("uniform_bonus_count");
    }
    return ResourceLocation.withDefaultNamespace("ore_drops");
  }

  private static void serializeFormulaParams(Formula formula, JsonObject parameters) {
    if (formula instanceof BinomialWithBonusCount binomial) {
      parameters.addProperty("extra", binomial.extraRounds());
      parameters.addProperty("probability", binomial.probability());
    } else if (formula instanceof UniformBonusCount uniform) {
      parameters.addProperty("bonusMultiplier", uniform.bonusMultiplier());
    }
  }
}
