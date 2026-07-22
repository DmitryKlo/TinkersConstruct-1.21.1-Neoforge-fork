package slimeknights.tconstruct.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/** Legacy TConstruct datapack condition backed by common config boolean values. */
public record ConfigCondition(String prop) implements ICondition {
  public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    Codec.STRING.fieldOf("prop").forGetter(ConfigCondition::prop)
  ).apply(instance, ConfigCondition::new));

  private static final Map<String,Supplier<Boolean>> PROPERTIES = Map.ofEntries(
    Map.entry("gravel_to_flint", () -> Config.COMMON.addGravelToFlintRecipe.get()),
    Map.entry("add_gravel_to_flint_recipe", () -> Config.COMMON.addGravelToFlintRecipe.get()),
    Map.entry("cheaper_netherite_alloy", () -> Config.COMMON.cheaperNetheriteAlloy.get()),
    Map.entry("wither_bone_drop", () -> Config.COMMON.witherBoneDrop.get()),
    Map.entry("slime_recipe_fix", () -> Config.COMMON.slimeRecipeFix.get()),
    Map.entry("glass_recipe_fix", () -> Config.COMMON.glassRecipeFix.get()),
    Map.entry("slimy_loot", () -> Config.COMMON.slimyLootChests.get()),
    Map.entry("allow_ingotless_alloys", () -> Config.COMMON.allowIngotlessAlloys.get()),
    Map.entry("allow_monster_melee_modifiers", () -> Config.COMMON.allowMonsterMeleeModifiers.get()),
    Map.entry("force_integration_materials", () -> Config.COMMON.forceIntegrationMaterials.get()),
    Map.entry("disable_side_inventory_whitelist", () -> Config.COMMON.disableSideInventoryWhitelist.get()),
    Map.entry("quick_apply_tool_modifiers_survival", () -> Config.COMMON.quickApplyToolModifiersSurvival.get())
  );

  @Override
  public boolean test(IContext context) {
    Supplier<Boolean> value = PROPERTIES.get(prop.toLowerCase(Locale.ROOT));
    if (value == null) {
      TConstruct.LOG.warn("Unknown tconstruct:config condition property '{}'", prop);
      return false;
    }
    return value.get();
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }
}
