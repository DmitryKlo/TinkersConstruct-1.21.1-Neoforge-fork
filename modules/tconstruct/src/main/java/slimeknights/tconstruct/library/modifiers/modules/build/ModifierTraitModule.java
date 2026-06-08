package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierTraitHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierTraitHook.TraitBuilder;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;

import java.util.List;

@RequiredArgsConstructor
public final class ModifierTraitModule implements ModifierTraitHook, ModifierModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ModifierTraitModule>defaultHooks(ModifierHooks.MODIFIER_TRAITS);
  public static final RecordLoadable<ModifierTraitModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("modifier", m -> m.modifier),
    IntLoadable.FROM_ZERO.requiredField("level", m -> m.level),
    ModifierTraitModule::new);

  private final ResourceLocation modifier;
  private final int level;

  public ModifierTraitModule(ResourceLocation modifier, int level, boolean required) {
    this(modifier, level);
  }

  public ModifierTraitModule(ModifierId modifier, int level, boolean required) {
    this(modifier.getLocation(), level);
  }

  public ModifierTraitModule(ModifierId modifier, int level, boolean required, Object condition) {
    this(modifier.getLocation(), level);
  }

  public static ModifierTraitModule tagCondition(ModifierId modifier, Object tag) {
    return new ModifierTraitModule(modifier.getLocation(), 1);
  }

  @Override
  public RecordLoadable<ModifierTraitModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addTraits(IToolContext context, ModifierEntry modifier, TraitBuilder builder, boolean firstEncounter) {
    if (firstEncounter) {
      builder.add(new ModifierId(this.modifier), level);
    }
  }
}
