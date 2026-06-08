package slimeknights.tconstruct.fluids.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import slimeknights.mantle.util.PotionHelper;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

/** Implements filling a bucket with an NBT fluid */
public class PotionBucketItem extends PotionItem {
  private final Supplier<? extends Fluid> supplier;
  public PotionBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
    super(builder);
    this.supplier = supplier;
  }

  public Fluid getFluid() {
    return supplier.get();
  }

  @Override
  public String getDescriptionId(ItemStack stack) {
    Potion potion = PotionHelper.getPotion(stack);
    if (potion == null) {
      return super.getDescriptionId();
    }
    String bucketKey = PotionHelper.getPotionName(potion, getDescriptionId() + ".effect.");
    if (Util.canTranslate(bucketKey)) {
      return bucketKey;
    }
    return super.getDescriptionId();
  }

  @Override
  public Component getName(ItemStack stack) {
    Potion potion = PotionHelper.getPotion(stack);
    if (potion == null) {
      return super.getName(stack);
    }
    String bucketKey = PotionHelper.getPotionName(potion, getDescriptionId() + ".effect.");
    if (Util.canTranslate(bucketKey)) {
      return Component.translatable(bucketKey);
    }
    return Component.translatable(getDescriptionId() + ".contents", Component.translatable(PotionHelper.getPotionName(potion, "item.minecraft.potion.effect.")));
  }

  @Override
  public ItemStack getDefaultInstance() {
    return PotionHelper.setPotion(new ItemStack(this), Potions.AWKWARD);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    Player player = living instanceof Player p ? p : null;
    if (player instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
    }

    if (!level.isClientSide) {
      for (MobEffectInstance effect : PotionHelper.getMobEffects(stack)) {
        if (effect.getEffect().value().isInstantenous()) {
          effect.getEffect().value().applyInstantenousEffect(player, player, living, effect.getAmplifier(), 2.5D);
        } else {
          living.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration() * 5 / 2, effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
        }
      }
    }

    if (player != null) {
      player.awardStat(Stats.ITEM_USED.get(this));
      if (!player.getAbilities().instabuild) {
        stack.shrink(1);
      }
    }

    if (player == null || !player.getAbilities().instabuild) {
      if (stack.isEmpty()) {
        return new ItemStack(Items.BUCKET);
      }
      if (player != null) {
        player.getInventory().add(new ItemStack(Items.BUCKET));
      }
    }
    living.gameEvent(GameEvent.DRINK);
    return stack;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    PotionHelper.addPotionTooltip(stack, tooltip, 2.5f);
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return 96;
  }

  public PotionBucketWrapper createFluidHandler(ItemStack stack, Void context) {
    return new PotionBucketWrapper(stack);
  }

  public static class PotionBucketWrapper extends FluidBucketWrapper {
    public PotionBucketWrapper(ItemStack container) {
      super(container);
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
      return PotionHelper.withTag(
        new FluidStack(((PotionBucketItem) container.getItem()).getFluid(), FluidType.BUCKET_VOLUME),
        PotionHelper.toTag(container));
    }
  }
}
