package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.RequiredArgsConstructor;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.ModuleHook;

import java.util.List;

@RequiredArgsConstructor
public final class ModifierRequirementsModule implements ModifierModule {
  public static final RecordLoadable<ModifierRequirementsModule> LOADER = RecordLoadable.singleton(new ModifierRequirementsModule());
  public static Builder builder() { return new Builder(); }

  @Override
  public RecordLoadable<ModifierRequirementsModule> getLoader() { return LOADER; }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() { return List.of(); }

  public static final class Builder {
    public Builder requirement(Object requirement) { return this; }
    public Builder requireModifier(Object modifier, int level) { return this; }
    public Builder displayModifier(Object modifier, int level) { return this; }
    public Builder modifierKey(Object key) { return this; }
    public ModifierRequirementsModule build() { return new ModifierRequirementsModule(); }
  }
}
