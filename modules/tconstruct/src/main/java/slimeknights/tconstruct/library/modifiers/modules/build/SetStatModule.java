package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.RequiredArgsConstructor;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import java.util.List;

@RequiredArgsConstructor
public final class SetStatModule implements ToolStatsModifierHook, ModifierModule {
  public static final RecordLoadable<SetStatModule> LOADER = RecordLoadable.singleton(new SetStatModule(null, null));
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<SetStatModule>defaultHooks(ModifierHooks.TOOL_STATS);

  private final IToolStat<?> stat;
  private final Object value;

  public static <T> Builder<T> set(IToolStat<T> stat) {
    return new Builder<>(stat);
  }

  @Override
  public RecordLoadable<SetStatModule> getLoader() { return LOADER; }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() { return DEFAULT_HOOKS; }

  @Override
  @SuppressWarnings("unchecked")
  public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
    if (stat != null && value != null) {
      ((IToolStat<Object>) stat).update(builder, value);
    }
  }

  public static final class Builder<T> {
    private final IToolStat<T> stat;
    private T value;

    private Builder(IToolStat<T> stat) { this.stat = stat; }

    public Builder<T> value(T value) { this.value = value; return this; }
    public SetStatModule build() { return new SetStatModule(stat, value); }
  }
}
