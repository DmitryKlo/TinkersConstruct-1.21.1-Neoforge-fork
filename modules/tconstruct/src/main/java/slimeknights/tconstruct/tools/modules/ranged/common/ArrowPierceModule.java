package slimeknights.tconstruct.tools.modules.ranged.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingInt;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

/** Module implementing the arrow pierce modifier */
public record ArrowPierceModule(LevelingInt amount, ModifierCondition<IToolStackView> condition) implements ModifierModule, ProjectileLaunchModifierHook.NoShooter, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ArrowPierceModule>defaultHooks(ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT);
  public static final RecordLoadable<ArrowPierceModule> LOADER = RecordLoadable.create(LevelingInt.LOADABLE.directField(ArrowPierceModule::amount), ModifierCondition.TOOL_FIELD, ArrowPierceModule::new);
  private static final Method SET_PIERCE_LEVEL = findSetPierceLevel();

  private static Method findSetPierceLevel() {
    try {
      Method method = AbstractArrow.class.getDeclaredMethod("setPierceLevel", byte.class);
      method.setAccessible(true);
      return method;
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  @Override
  public RecordLoadable<ArrowPierceModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onProjectileShoot(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (condition.matches(tool, modifier) && arrow != null) {
      int amount = this.amount.compute(modifier.getEffectiveLevel());
      if (amount > 0) {
        if (SET_PIERCE_LEVEL != null) {
          try {
            SET_PIERCE_LEVEL.invoke(arrow, (byte) amount);
          } catch (ReflectiveOperationException ignored) {
            // NeoForge 1.21.1 makes the vanilla setter private; failing here just leaves vanilla arrows unpierced.
          }
        }
      }
    }
  }
}
