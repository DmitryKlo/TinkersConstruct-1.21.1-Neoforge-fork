package slimeknights.tconstruct.library.tools.helper;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.bus.api.EventPriority;
import slimeknights.tconstruct.common.TinkerDamageTypes;
import slimeknights.tconstruct.common.TinkerEffect;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.hook.combat.ArmorLootingModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.LootingModifierHook;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.context.LootingContext;
import slimeknights.tconstruct.library.tools.nbt.DummyToolStack;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.shared.TinkerEffects;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Logic to handle the looting event for all main tinker tools
 */
public class ModifierLootingHandler {
  /** If contained in the set, they should use the offhand for looting */
  private static final Map<UUID,EquipmentSlot> LOOTING_OFFHAND = new HashMap<>();
  /** Looting level to apply during the current entity death, set between {@link LivingDeathEvent} and {@link LivingDropsEvent} */
  @Nullable
  private static ThreadLocal<Integer> activeLootingLevel = new ThreadLocal<>();
  private static boolean init = false;

  /** Initializies this listener */
  public static void init() {
    if (init) {
      return;
    }
    init = true;
    // we overwrite looting values from vanilla in a couple cases, but mod effects that globally boost looting should still boost us
    NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ModifierLootingHandler::onDeath);
    NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ModifierLootingHandler::onGetEnchantmentLevel);
    NeoForge.EVENT_BUS.addListener(EventPriority.LOW, ModifierLootingHandler::onDrops);
    NeoForge.EVENT_BUS.addListener(ModifierLootingHandler::onLeaveServer);
  }

  /**
   * Sets the hand used for looting, so the tool is fetched from the proper context
   * @param entity    Player to set
   * @param slotType  Slot type
   */
  public static void setLootingSlot(LivingEntity entity, EquipmentSlot slotType) {
    if (slotType == EquipmentSlot.MAINHAND) {
      LOOTING_OFFHAND.remove(entity.getUUID());
    } else {
      LOOTING_OFFHAND.put(entity.getUUID(), slotType);
    }
  }

  /** Gets the slot to use for looting */
  public static EquipmentSlot getLootingSlot(@Nullable LivingEntity entity) {
    return entity != null ? LOOTING_OFFHAND.getOrDefault(entity.getUUID(), EquipmentSlot.MAINHAND) : EquipmentSlot.MAINHAND;
  }

  /** Computes the looting level for the given death */
  private static int computeLootingLevel(LivingEntity target, DamageSource damageSource) {
    // bleeding kills use the level of the effect for looting
    if (damageSource.is(TinkerDamageTypes.BLEEDING)) {
      return Math.max(0, TinkerEffect.getAmplifier(target, TinkerEffects.holder(TinkerEffects.bleeding)));
    }

    Entity source = damageSource.getEntity();
    if (!(source instanceof LivingEntity holder)) {
      return 0;
    }

    Entity direct = damageSource.getDirectEntity();
    int level = 0;

    // determine who is in charge of the looting
    LootingContext context;
    IToolStackView tool = null;
    if (direct instanceof Projectile) {
      // need to build a context from the relevant capabilities to use the modifier
      ModifierNBT modifiers = EntityModifierCapability.getOrEmpty(direct);
      context = new LootingContext(holder, target, damageSource, null);
      // no modifiers means its not a projectile we fired, so just defer to dumb vanilla behavior of whatever looting
      // since we don't set the enchantment on our tools, our looting modifiers won't set anything here anyways
      if (!modifiers.isEmpty()) {
        ModDataNBT persistentData = direct.getCapability(PersistentDataCapability.CAPABILITY);
        if (persistentData == null) {
          persistentData = new ModDataNBT();
        }
        level = LootingModifierHook.getLooting(new DummyToolStack(Items.AIR, modifiers, persistentData), context, 0);
      }
    } else {
      // not an arrow? means the held tool is to blame
      EquipmentSlot slotType = getLootingSlot(holder);
      context = new LootingContext(holder, target, damageSource, slotType);
      ItemStack held = holder.getItemBySlot(slotType);

      // if its modifiable, let it increase the level
      if (held.is(TinkerTags.Items.MODIFIABLE)) {
        tool = ToolStack.from(held);
        level = LootingModifierHook.getLooting(tool, context, level);
      } else if (slotType != EquipmentSlot.MAINHAND) {
        // if it's not modifiable, yet we have a lot marked to blame for looting, ignore the event value
        level = 0;
      }
    }
    // boost looting with armor regardless, hopefully you did not switch your pants mid arrow firing
    level = ArmorLootingModifierHook.getLooting(tool, context, level);
    // we allow the hook to return negatives to cancel out looting, so ensure its at least 0
    return Math.max(level, 0);
  }

  /** Stores the looting level to inject during loot generation */
  private static void onDeath(LivingDeathEvent event) {
    activeLootingLevel.set(computeLootingLevel(event.getEntity(), event.getSource()));
  }

  /** Injects modifier looting during loot table generation */
  private static void onGetEnchantmentLevel(GetEnchantmentLevelEvent event) {
    Integer level = activeLootingLevel.get();
    if (level == null || !event.isTargetting(Enchantments.LOOTING)) {
      return;
    }
    event.getHolder(Enchantments.LOOTING).ifPresent(holder -> event.getEnchantments().set(holder, level));
  }

  /** Clears stored looting level after drops are generated */
  private static void onDrops(LivingDropsEvent event) {
    activeLootingLevel.remove();
  }

  /** Called when a player leaves the server to clear the face */
  private static void onLeaveServer(PlayerLoggedOutEvent event) {
    LOOTING_OFFHAND.remove(event.getEntity().getUUID());
  }
}

