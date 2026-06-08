package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.RequiredArgsConstructor;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingInt;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.LevelingIntModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.data.predicate.IJsonPredicate;

import java.util.List;

/** Module to boost tool stats */
public final class StatBoostModule implements ToolStatsModifierHook, ModifierModule, LevelingIntModule {
  public static final RecordLoadable<StatBoostModule> LOADER = RecordLoadable.singleton(new StatBoostModule(null, LevelingInt.ZERO, Op.ADD, false));
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<StatBoostModule>defaultHooks(ModifierHooks.TOOL_STATS);

  private final IToolStat<?> stat;
  private final LevelingInt level;
  private final Op op;
  private final boolean conditional;

  private enum Op { ADD, MULTIPLY_BASE, MULTIPLY }

  private StatBoostModule(IToolStat<?> stat, LevelingInt level, Op op, boolean conditional) {
    this.stat = stat;
    this.level = level;
    this.op = op;
    this.conditional = conditional;
  }

  public static Builder add(IToolStat<?> stat) { return new Builder(stat, Op.ADD, false); }
  public static Builder multiplyBase(INumericToolStat<?> stat) { return new Builder(stat, Op.MULTIPLY_BASE, false); }
  public static Builder multiplyConditional(INumericToolStat<?> stat) { return new Builder(stat, Op.MULTIPLY, true); }
  public static Builder multiplyAll(INumericToolStat<?> stat) { return new Builder(stat, Op.MULTIPLY, false); }

  @Override
  public LevelingInt level() { return level; }

  @Override
  public RecordLoadable<StatBoostModule> getLoader() { return LOADER; }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() { return DEFAULT_HOOKS; }

  @Override
  public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
    if (stat instanceof INumericToolStat<?> numeric) {
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
    private final boolean conditional;
    private LevelingInt level = LevelingInt.ZERO;

    private Builder(IToolStat<?> stat, Op op, boolean conditional) {
      this.stat = stat;
      this.op = op;
      this.conditional = conditional;
    }

    public Builder flat(float flat) { level = LevelingInt.flat((int) flat); return this; }
    public Builder eachLevel(float each) { level = LevelingInt.eachLevel((int) each); return this; }
    public Builder amount(float flat, float each) { level = new LevelingInt((int) flat, (int) each); return this; }
    public Builder toolItem(IJsonPredicate<Item> predicate) { return this; }
    public Builder toolTag(TagKey<Item> tag) { return this; }
    public Builder levelRange(int min, int max) { return this; }
    public Builder minLevel(int min) { return this; }
    public Builder maxLevel(int max) { return this; }
    public StatBoostModule build() { return new StatBoostModule(stat, level, op, conditional); }
  }
}
