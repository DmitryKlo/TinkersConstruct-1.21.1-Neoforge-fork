package slimeknights.tconstruct.library.modifiers.modules.build;

import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.ModuleHook;

import java.util.List;

public final class EnchantmentModule implements ModifierModule {
  public static final RecordLoadable<EnchantmentModule> LOADER = RecordLoadable.singleton(new EnchantmentModule());
  public static Builder builder(Object enchantment) { return new Builder(); }

  @Override
  public RecordLoadable<EnchantmentModule> getLoader() { return LOADER; }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() { return List.of(); }

  public static final class Builder {
    public Builder toolItem(Object predicate) { return this; }
    public Builder lootingLevel(Object level) { return this; }
    public EnchantmentModule constant() { return new EnchantmentModule(); }
    public EnchantmentModule armorHarvest(Object slots) { return new EnchantmentModule(); }
    public EnchantmentModule protection() { return new EnchantmentModule(); }
  }
}
