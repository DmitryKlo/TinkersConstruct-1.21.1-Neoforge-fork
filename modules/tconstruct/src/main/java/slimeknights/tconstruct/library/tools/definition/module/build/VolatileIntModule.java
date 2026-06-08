package slimeknights.tconstruct.library.tools.definition.module.build;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingInt;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

import java.util.List;

public final class VolatileIntModule implements VolatileDataToolHook, ToolModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileIntModule>defaultHooks(ToolHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileIntModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("key", m -> m.key),
    LevelingInt.LOADABLE.requiredField("level", m -> m.level),
    VolatileIntModule::new);

  private final ResourceLocation key;
  private final LevelingInt level;

  public VolatileIntModule(ResourceLocation key, int value) {
    this(key, LevelingInt.flat(value));
  }

  public VolatileIntModule(ResourceLocation key, LevelingInt level) {
    this.key = key;
    this.level = level;
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
  public void addVolatileData(IToolContext context, ToolDataNBT volatileData) {
    volatileData.putInt(key, level.compute(1));
  }
}
