package slimeknights.tconstruct.library.tools.definition.module.build;

import com.google.common.collect.ImmutableMap;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

import java.util.List;
import java.util.Map;

public final class ToolSlotsModule implements VolatileDataToolHook, ToolModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ToolSlotsModule>defaultHooks(ToolHooks.VOLATILE_DATA);
  public static final RecordLoadable<ToolSlotsModule> LOADER = RecordLoadable.create(
    SlotType.LOADABLE.mapWithValues(IntLoadable.FROM_ZERO, 1).requiredField("slots", ToolSlotsModule::slots),
    ToolSlotsModule::new);

  private final Map<SlotType, Integer> slots;

  public Map<SlotType, Integer> slots() {
    return slots;
  }

  public ToolSlotsModule(Map<SlotType, Integer> slots) {
    this.slots = slots;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public RecordLoadable<ToolSlotsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addVolatileData(IToolContext context, ToolDataNBT volatileData) {
    for (Map.Entry<SlotType, Integer> entry : slots.entrySet()) {
      volatileData.setSlots(entry.getKey(), entry.getValue());
    }
  }

  public static final class Builder {
    private final ImmutableMap.Builder<SlotType, Integer> slots = ImmutableMap.builder();

    public Builder slots(SlotType type, int count) {
      slots.put(type, count);
      return this;
    }

    public ToolSlotsModule build() {
      return new ToolSlotsModule(slots.build());
    }
  }
}
