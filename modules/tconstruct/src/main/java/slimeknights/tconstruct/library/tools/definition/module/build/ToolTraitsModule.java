package slimeknights.tconstruct.library.tools.definition.module.build;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.helper.ModifierBuilder;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;

import java.util.List;

@RequiredArgsConstructor
public final class ToolTraitsModule implements ToolTraitHook, ToolModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ToolTraitsModule>defaultHooks(ToolHooks.TOOL_TRAITS);
  public static final RecordLoadable<ToolTraitsModule> LOADER = RecordLoadable.create(
    ModifierEntry.LOADABLE.list(0).requiredField("traits", ToolTraitsModule::traits),
    ToolTraitsModule::new);

  private final List<ModifierEntry> traits;

  public List<ModifierEntry> traits() {
    return traits;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public RecordLoadable<ToolTraitsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addTraits(ToolDefinition definition, MaterialNBT materials, ModifierBuilder builder) {
    builder.add(traits);
  }

  public static final class Builder {
    private final ImmutableList.Builder<ModifierEntry> traits = ImmutableList.builder();

    public Builder trait(ModifierId modifier, int level) {
      traits.add(new ModifierEntry(modifier, level));
      return this;
    }

    public Builder trait(ModifierId modifier) {
      return trait(modifier, 1);
    }

    public Builder trait(LazyModifier modifier, int level) {
      return trait(modifier.getModifierId(), level);
    }

    public Builder trait(LazyModifier modifier) {
      return trait(modifier.getModifierId(), 1);
    }

    public ToolTraitsModule build() {
      return new ToolTraitsModule(traits.build());
    }
  }
}
