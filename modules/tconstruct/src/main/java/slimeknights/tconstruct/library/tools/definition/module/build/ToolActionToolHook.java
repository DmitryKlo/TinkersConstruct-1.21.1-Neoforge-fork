package slimeknights.tconstruct.library.tools.definition.module.build;

import net.neoforged.neoforge.common.ItemAbility;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

/** Hook for checking if a tool can perform a given action from its definition */
public interface ToolActionToolHook {
  boolean canPerformAction(IToolStackView tool, ItemAbility toolAction);

  record AnyMerger(Collection<ToolActionToolHook> hooks) implements ToolActionToolHook {
    @Override
    public boolean canPerformAction(IToolStackView tool, ItemAbility toolAction) {
      for (ToolActionToolHook hook : hooks) {
        if (hook.canPerformAction(tool, toolAction)) {
          return true;
        }
      }
      return false;
    }
  }
}
