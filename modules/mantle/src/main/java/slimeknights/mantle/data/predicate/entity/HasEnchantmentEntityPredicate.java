package slimeknights.mantle.data.predicate.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.Optional;

/**
 * Predicate that checks if the given entity has the given enchantment on any of their equipment
 */
public record HasEnchantmentEntityPredicate(ResourceKey<Enchantment> enchantment) implements LivingEntityPredicate {
  public static final RecordLoadable<HasEnchantmentEntityPredicate> LOADER = RecordLoadable.create(
    Loadables.resourceKey(Registries.ENCHANTMENT).requiredField("enchantment", HasEnchantmentEntityPredicate::enchantment),
    HasEnchantmentEntityPredicate::new);

  @Override
  public boolean matches(LivingEntity entity) {
    Optional<Registry<Enchantment>> registry = entity.level().registryAccess().registry(Registries.ENCHANTMENT);
    if (registry.isEmpty()) {
      return false;
    }
    Optional<Holder.Reference<Enchantment>> holder = registry.get().getHolder(enchantment);
    return holder.isPresent() && EnchantmentHelper.getEnchantmentLevel(holder.get(), entity) > 0;
  }

  @Override
  public RecordLoadable<HasEnchantmentEntityPredicate> getLoader() {
    return LOADER;
  }
}
