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
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

/** Boosts drop rates based on modifier level */
public class ModifierBonusLootFunction extends LootItemConditionalFunction {
  public static final MapCodec<ModifierBonusLootFunction> CODEC = MapCodec.unit(() -> new ModifierBonusLootFunction(List.of(), ModifierId.EMPTY, oreDropsFormula(), true));

  /** Modifier ID to use for multiplier bonus */
  private final ModifierId modifier;
  /** Formula to apply */
  private final Formula formula;
  /** If true, considers level 1 as bonus, if false considers level 1 as no bonus */
  private final boolean includeBase;

  protected ModifierBonusLootFunction(List<LootItemCondition> conditions, ModifierId modifier, Formula formula, boolean includeBase) {
    super(conditions);
    this.modifier = modifier;
    this.formula = formula;
    this.includeBase = includeBase;
  }

  /** Creates a generic builder */
  public static Builder<?> builder(ModifierId modifier, Formula formula, boolean includeBase) {
    return simpleBuilder(conditions -> new ModifierBonusLootFunction(conditions, modifier, formula, includeBase));
  }

  /** Creates a builder for the binomial with bonus formula */
  public static Builder<?> binomialWithBonusCount(ModifierId modifier, float probability, int extra, boolean includeBase) {
    return builder(modifier, binomialFormula(extra, probability), includeBase);
  }

  /** Creates a builder for the ore drops formula */
  public static Builder<?> oreDrops(ModifierId modifier, boolean includeBase) {
    return builder(modifier, oreDropsFormula(), includeBase);
  }

  /** Creates a builder for the uniform bonus count */
  public static Builder<?> uniformBonusCount(ModifierId modifier, int bonusMultiplier, boolean includeBase) {
    return builder(modifier, uniformFormula(bonusMultiplier), includeBase);
  }

  @Override
  public LootItemFunctionType<ModifierBonusLootFunction> getType() {
    return TinkerModifiers.modifierBonusFunction.get();
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(LootContextParams.TOOL);
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    int level = ModifierUtil.getModifierLevel(context.getParam(LootContextParams.TOOL), modifier);
    if (!includeBase) {
      level--;
    }
    if (level > 0) {
      stack.setCount(formula.calculateNewCount(context.getRandom(), stack.getCount(), level));
    }
    return stack;
  }

  private static Formula parseFormula(ResourceLocation id, JsonObject parameters, JsonDeserializationContext context) {
    if (id.equals(BinomialWithBonusCount.TYPE)) {
      return binomialFormula(GsonHelper.getAsInt(parameters, "extra", 0), GsonHelper.getAsFloat(parameters, "probability", 0.5f));
    }
    if (id.equals(UniformBonusCount.TYPE)) {
      return uniformFormula(GsonHelper.getAsInt(parameters, "bonusMultiplier", 1));
    }
    if (id.equals(OreDrops.TYPE)) {
      return oreDropsFormula();
    }
    throw new JsonParseException("Invalid formula id: " + id);
  }

  private static Formula binomialFormula(int extra, float probability) {
    return createFormula(BinomialWithBonusCount.class, new Class<?>[] { int.class, float.class }, extra, probability);
  }

  private static Formula uniformFormula(int bonusMultiplier) {
    return createFormula(UniformBonusCount.class, new Class<?>[] { int.class }, bonusMultiplier);
  }

  private static Formula oreDropsFormula() {
    return createFormula(OreDrops.class, new Class<?>[0]);
  }

  private static Formula createFormula(Class<? extends Formula> type, Class<?>[] parameterTypes, Object... args) {
    try {
      Constructor<? extends Formula> constructor = type.getDeclaredConstructor(parameterTypes);
      constructor.setAccessible(true);
      return constructor.newInstance(args);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to create loot bonus formula " + type.getSimpleName(), e);
    }
  }

  /** Serializer class */
  public static class Serializer implements slimeknights.mantle.loot.legacy.LegacyLootSerializer<ModifierBonusLootFunction> {
    @Override
    public void serialize(JsonObject json, ModifierBonusLootFunction loot, JsonSerializationContext context) {
      json.addProperty("modifier", loot.modifier.toString());
      json.addProperty("formula", formulaId(loot.formula).toString());
      JsonObject parameters = new JsonObject();
      serializeFormulaParams(loot.formula, parameters);
      if (!parameters.isEmpty()) {
        json.add("parameters", parameters);
      }
      json.addProperty("include_base", loot.includeBase);
    }

    @Override
    public ModifierBonusLootFunction deserialize(JsonObject json, JsonDeserializationContext context) {
      ModifierId modifier = new ModifierId(JsonHelper.getResourceLocation(json, "modifier"));
      ResourceLocation id = JsonHelper.getResourceLocation(json, "formula");
      JsonObject parameters = json.has("parameters") ? GsonHelper.getAsJsonObject(json, "parameters") : new JsonObject();
      Formula formula = parseFormula(id, parameters, context);
      boolean includeBase = GsonHelper.getAsBoolean(json, "include_base", true);
      return new ModifierBonusLootFunction(List.of(), modifier, formula, includeBase);
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
