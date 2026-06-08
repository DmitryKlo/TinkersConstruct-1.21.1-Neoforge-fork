package slimeknights.tconstruct.library.tools.definition.module.build;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ArmorItem;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.MultiplierNBT;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.tools.modules.ArmorModuleBuilder;

import java.util.List;

@RequiredArgsConstructor
public final class MultiplyStatsModule implements ToolStatsHook, ToolModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MultiplyStatsModule>defaultHooks(ToolHooks.TOOL_STATS);
  public static final RecordLoadable<MultiplyStatsModule> LOADER = RecordLoadable.create(
    MultiplierNBT.LOADABLE.requiredField("multipliers", MultiplyStatsModule::multipliers),
    MultiplyStatsModule::new);

  private final MultiplierNBT multipliers;

  public MultiplierNBT multipliers() {
    return multipliers;
  }

  public static ArmorBuilder armor(List<ArmorItem.Type> slotTypes) {
    return new ArmorBuilder(slotTypes);
  }

  @Override
  public RecordLoadable<MultiplyStatsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addToolStats(IToolContext context, ModifierStatsBuilder builder) {
    for (INumericToolStat<?> stat : multipliers.getContainedStats()) {
      builder.multiplier(stat, multipliers.get(stat));
    }
  }

  public static class ArmorBuilder implements ArmorModuleBuilder<MultiplyStatsModule> {
    private final List<ArmorItem.Type> slotTypes;
    private final MultiplierNBT.Builder[] builders = new MultiplierNBT.Builder[4];

    private ArmorBuilder(List<ArmorItem.Type> slotTypes) {
      this.slotTypes = slotTypes;
      for (ArmorItem.Type slotType : slotTypes) {
        builders[slotType.ordinal()] = MultiplierNBT.builder();
      }
    }

    private MultiplierNBT.Builder getBuilder(ArmorItem.Type slotType) {
      MultiplierNBT.Builder builder = builders[slotType.ordinal()];
      if (builder == null) {
        throw new IllegalArgumentException("Unsupported slot type " + slotType);
      }
      return builder;
    }

    public ArmorBuilder set(ArmorItem.Type slotType, INumericToolStat<?> stat, float multiplier) {
      getBuilder(slotType).set(stat, multiplier);
      return this;
    }

    public ArmorBuilder setAll(INumericToolStat<?> stat, float multiplier) {
      for (ArmorItem.Type slotType : slotTypes) {
        getBuilder(slotType).set(stat, multiplier);
      }
      return this;
    }

    @Override
    public MultiplyStatsModule build(ArmorItem.Type slot) {
      return new MultiplyStatsModule(getBuilder(slot).build());
    }
  }
}
