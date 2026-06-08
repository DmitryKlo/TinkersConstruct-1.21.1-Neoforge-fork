package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingInt;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.LevelingIntModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

import java.util.List;

@RequiredArgsConstructor
public final class VolatileIntModule implements VolatileDataModifierHook, ModifierModule, LevelingIntModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileIntModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileIntModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("key", m -> m.key),
    LevelingIntModule.FIELD,
    VolatileIntModule::new);

  private final ResourceLocation key;
  private final LevelingInt level;

  @Override
  public LevelingInt level() {
    return level;
  }

  @Override
  public RecordLoadable<VolatileIntModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    volatileData.putInt(key, getLevel(modifier));
  }
}
