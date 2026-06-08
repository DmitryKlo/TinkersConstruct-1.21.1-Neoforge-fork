package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierTraitHook.TraitBuilder;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;

import java.util.List;

/** Swaps traits from one hook to another based on persistent variant data */
@RequiredArgsConstructor
public final class SwappableToolTraitsModule implements ModifierModule {
  public static final RecordLoadable<SwappableToolTraitsModule> LOADER = RecordLoadable.singleton(new SwappableToolTraitsModule(null, "", ToolHooks.TOOL_TRAITS));
  private final ResourceLocation key;
  private final String variant;
  private final ModuleHook<?> targetHook;

  @Override
  public RecordLoadable<SwappableToolTraitsModule> getLoader() { return LOADER; }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() { return List.of(); }

  @Override
  public void addModules(ModuleHookMap.Builder builder) {}
}
