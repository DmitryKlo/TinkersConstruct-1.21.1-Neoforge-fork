package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.StackDataHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

import java.util.List;

@RequiredArgsConstructor
public final class RarityModule implements VolatileDataModifierHook, ModifierModule {
  public static final ResourceLocation RARITY = TConstruct.getResource("rarity");
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<RarityModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<RarityModule> LOADER = RecordLoadable.singleton(new RarityModule(Rarity.COMMON));

  private final Rarity rarity;

  public static void setRarity(ModDataNBT volatileData, Rarity rarity) {
    volatileData.putInt(RARITY, rarity.ordinal());
  }

  public static Rarity getRarity(ItemStack stack) {
    if (!StackDataHelper.hasTag(stack)) {
      return Rarity.COMMON;
    }
    int ordinal = ModifierUtil.getVolatileInt(stack, RARITY);
    Rarity[] values = Rarity.values();
    if (ordinal >= 0 && ordinal < values.length) {
      return values[ordinal];
    }
    return Rarity.COMMON;
  }

  @Override
  public RecordLoadable<RarityModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    setRarity(volatileData, rarity);
  }
}
