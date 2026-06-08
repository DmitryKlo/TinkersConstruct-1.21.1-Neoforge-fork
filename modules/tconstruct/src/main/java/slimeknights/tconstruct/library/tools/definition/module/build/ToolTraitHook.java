package slimeknights.tconstruct.library.tools.definition.module.build;

import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.helper.ModifierBuilder;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.Collection;

/** Hook for fetching tool traits from a tool definition */
public interface ToolTraitHook {
  void addTraits(ToolDefinition definition, MaterialNBT materials, ModifierBuilder builder);

  static ModifierNBT getTraits(ToolDefinition definition, MaterialNBT materials) {
    ModifierNBT.Builder builder = ModifierNBT.builder();
    definition.getData().getHook(ToolHooks.TOOL_TRAITS).addTraits(definition, materials, builder);
    return builder.build();
  }

  record AllMerger(Collection<ToolTraitHook> hooks) implements ToolTraitHook {
    @Override
    public void addTraits(ToolDefinition definition, MaterialNBT materials, ModifierBuilder builder) {
      for (ToolTraitHook hook : hooks) {
        hook.addTraits(definition, materials, builder);
      }
    }
  }
}
