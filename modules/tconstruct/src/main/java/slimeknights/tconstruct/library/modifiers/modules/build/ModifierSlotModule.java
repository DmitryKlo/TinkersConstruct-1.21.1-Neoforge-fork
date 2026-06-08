package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.RequiredArgsConstructor;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingInt;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.LevelingIntModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;
import slimeknights.mantle.data.predicate.IJsonPredicate;

import java.util.List;

@RequiredArgsConstructor
public final class ModifierSlotModule implements VolatileDataModifierHook, ModifierModule, LevelingIntModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ModifierSlotModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<ModifierSlotModule> LOADER = RecordLoadable.create(
    SlotType.LOADABLE.requiredField("slot", m -> m.slot),
    LevelingIntModule.FIELD,
    ModifierSlotModule::new);

  private final SlotType slot;
  private final LevelingInt level;

  public static Builder slot(SlotType slot) {
    return new Builder(slot);
  }

  @Override
  public LevelingInt level() {
    return level;
  }

  @Override
  public RecordLoadable<ModifierSlotModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    volatileData.addSlots(slot, getLevel(modifier));
  }

  public static final class Builder {
    private final SlotType slot;
    private LevelingInt level = LevelingInt.LEVEL;

    private Builder(SlotType slot) {
      this.slot = slot;
    }

    public Builder eachLevel(int eachLevel) {
      this.level = LevelingInt.eachLevel(eachLevel);
      return this;
    }

    public Builder flat(int flat) {
      this.level = LevelingInt.flat(flat);
      return this;
    }

    public Builder toolContext(IJsonPredicate<IToolContext> tool) {
      return this;
    }

    public ModifierSlotModule build() {
      return new ModifierSlotModule(slot, level);
    }
  }
}
