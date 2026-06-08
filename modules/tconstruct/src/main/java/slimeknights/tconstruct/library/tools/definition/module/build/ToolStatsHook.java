package slimeknights.tconstruct.library.tools.definition.module.build;

import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import java.util.Collection;

/** Hook for adding raw unconditional stats to a tool from its definition */
public interface ToolStatsHook {
  void addToolStats(IToolContext context, ModifierStatsBuilder builder);

  record AllMerger(Collection<ToolStatsHook> hooks) implements ToolStatsHook {
    @Override
    public void addToolStats(IToolContext context, ModifierStatsBuilder builder) {
      for (ToolStatsHook hook : hooks) {
        hook.addToolStats(context, builder);
      }
    }
  }
}
