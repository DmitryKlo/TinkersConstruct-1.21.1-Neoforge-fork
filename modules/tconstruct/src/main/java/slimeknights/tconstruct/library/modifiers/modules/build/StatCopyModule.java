package slimeknights.tconstruct.library.modifiers.modules.build;

import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.ModuleHook;

import java.util.List;

public final class StatCopyModule implements ModifierModule {
  public static final RecordLoadable<StatCopyModule> LOADER = RecordLoadable.singleton(new StatCopyModule());
  public static Builder builder(Object from, Object to) { return new Builder(); }

  @Override
  public RecordLoadable<StatCopyModule> getLoader() { return LOADER; }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() { return List.of(); }

  public static final class Builder {
    public Builder eachLevel(float value) { return this; }
    public StatCopyModule build() { return new StatCopyModule(); }
  }
}
