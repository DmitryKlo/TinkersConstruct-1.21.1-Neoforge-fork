package slimeknights.tconstruct.library.tools.definition.module.build;

import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

import java.util.Collection;

/** Hook to add volatile data to a tool from its definition */
public interface VolatileDataToolHook {
  void addVolatileData(IToolContext context, ToolDataNBT volatileData);

  record AllMerger(Collection<VolatileDataToolHook> hooks) implements VolatileDataToolHook {
    @Override
    public void addVolatileData(IToolContext context, ToolDataNBT volatileData) {
      for (VolatileDataToolHook hook : hooks) {
        hook.addVolatileData(context, volatileData);
      }
    }
  }
}
