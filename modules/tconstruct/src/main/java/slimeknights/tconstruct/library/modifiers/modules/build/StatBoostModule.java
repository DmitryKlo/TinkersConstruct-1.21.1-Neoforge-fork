package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.RequiredArgsConstructor;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingValue;
import slimeknights.tconstruct.library.json.predicate.tool.ToolContextPredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.data.predicate.IJsonPredicate;

import java.util.List;

/** Module to boost tool stats */
public final class StatBoostModule implements ToolStatsModifierHook, ModifierModule, ConditionalModule<IToolContext> {
  private static final StringLoadable<Op> OP_LOADABLE = StringLoadable.DEFAULT.xmap((name, error) -> switch (name) {
    case "add", "addition", "add_value" -> Op.ADD;
    case "multiply_base", "add_multiplied_base" -> Op.MULTIPLY_BASE;
    case "multiply_all", "multiply_total", "multiply_conditional", "add_multiplied_total" -> Op.MULTIPLY;
    default -> throw error.create("Invalid stat boost operation " + name);
  }, (operation, error) -> switch (operation) {
    case ADD -> "add";
    case MULTIPLY_BASE -> "multiply_base";
    case MULTIPLY -> "multiply_all";
  });
  public static final RecordLoadable<StatBoostModule> LOADER = RecordLoadable.create(
    ToolStats.LOADER.requiredField("stat", StatBoostModule::stat),
    LevelingValue.LOADABLE.directField(StatBoostModule::level),
    OP_LOADABLE.defaultField("operation", Op.ADD, StatBoostModule::op),
    ModifierCondition.CONTEXT_FIELD,
    StatBoostModule::new);
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<StatBoostModule>defaultHooks(ModifierHooks.TOOL_STATS);

  private final IToolStat<?> stat;
  private final LevelingValue level;
  private final Op op;
  private final ModifierCondition<IToolContext> condition;

  private enum Op { ADD, MULTIPLY_BASE, MULTIPLY }

  private StatBoostModule(IToolStat<?> stat, LevelingValue level, Op op, ModifierCondition<IToolContext> condition) {
    this.stat = stat;
    this.level = level;
    this.op = op;
    this.condition = condition;
  }

  public static Builder add(IToolStat<?> stat) { return new Builder(stat, Op.ADD); }
  public static Builder multiplyBase(INumericToolStat<?> stat) { return new Builder(stat, Op.MULTIPLY_BASE); }
  public static Builder multiplyConditional(INumericToolStat<?> stat) { return new Builder(stat, Op.MULTIPLY); }
  public static Builder multiplyAll(INumericToolStat<?> stat) { return new Builder(stat, Op.MULTIPLY); }

  @Override
  public ModifierCondition<IToolContext> condition() { return condition; }

  public IToolStat<?> stat() { return stat; }

  public LevelingValue level() { return level; }

  private Op op() { return op; }

  @Override
  public RecordLoadable<StatBoostModule> getLoader() { return LOADER; }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() { return DEFAULT_HOOKS; }

  @Override
  public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
    if (condition.matches(context, modifier) && stat instanceof INumericToolStat<?> numeric) {
      float value = level.compute(modifier.getEffectiveLevel());
      switch (op) {
        case ADD -> numeric.add(builder, value);
        case MULTIPLY_BASE, MULTIPLY -> builder.multiplier(numeric, value);
      }
    }
  }

  public static final class Builder {
    private final IToolStat<?> stat;
    private final Op op;
    private ModifierCondition<IToolContext> condition = ModifierCondition.ANY_CONTEXT;
    private LevelingValue level = LevelingValue.ZERO;

    private Builder(IToolStat<?> stat, Op op) {
      this.stat = stat;
      this.op = op;
    }

    public Builder flat(float flat) { level = LevelingValue.flat(flat); return this; }
    public Builder eachLevel(float each) { level = LevelingValue.eachLevel(each); return this; }
    public Builder amount(float flat, float each) { level = new LevelingValue(flat, each); return this; }
    public Builder toolItem(IJsonPredicate<Item> predicate) { condition = condition.with(ToolContextPredicate.fallback(predicate)); return this; }
    public Builder toolTag(TagKey<Item> tag) { return toolItem(slimeknights.mantle.data.predicate.item.ItemPredicate.tag(tag)); }
    public Builder levelRange(int min, int max) { condition = condition.with(ModifierEntry.VALID_LEVEL.range(min, max)); return this; }
    public Builder minLevel(int min) { condition = condition.minLevel(min); return this; }
    public Builder maxLevel(int max) { condition = condition.maxLevel(max); return this; }
    public StatBoostModule build() { return new StatBoostModule(stat, level, op, condition); }
  }
}
