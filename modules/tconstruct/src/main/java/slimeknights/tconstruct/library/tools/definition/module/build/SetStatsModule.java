package slimeknights.tconstruct.library.tools.definition.module.build;

import lombok.RequiredArgsConstructor;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import java.util.List;

@RequiredArgsConstructor
public final class SetStatsModule implements ToolStatsHook, ToolModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<SetStatsModule>defaultHooks(ToolHooks.TOOL_STATS);
  public static final RecordLoadable<SetStatsModule> LOADER = RecordLoadable.create(
    StatsNBT.LOADABLE.requiredField("stats", SetStatsModule::stats),
    SetStatsModule::new);

  private final StatsNBT stats;

  public StatsNBT stats() {
    return stats;
  }

  @Override
  public RecordLoadable<SetStatsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addToolStats(IToolContext context, ModifierStatsBuilder builder) {
    for (IToolStat<?> stat : stats.getContainedStats()) {
      applyStat(builder, stat);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void applyStat(ModifierStatsBuilder builder, IToolStat<?> stat) {
    IToolStat<T> typed = (IToolStat<T>) stat;
    typed.update(builder, stats.get(typed));
  }
}
