package slimeknights.tconstruct.tools.entity;

import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.EntityInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedContext;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ScheduledProjectileTaskModifierHook;
import slimeknights.tconstruct.library.tools.IndestructibleItemEntity;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.display.ToolNameHook;
import slimeknights.tconstruct.library.tools.definition.module.mining.IsEffectiveToolHook;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.Schedule;
import slimeknights.tconstruct.shared.TinkerEffects;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.modifiers.effect.MagneticEffect;

import javax.annotation.Nullable;

/** Based on {@link net.minecraft.world.entity.projectile.ThrownTrident} for throwing a modifiable weapon. */
public class ThrownTool extends ThrownTrident implements ToolProjectile {
  /** Key to sync the stack to the client */
  protected static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(ThrownTool.class, EntityDataSerializers.ITEM_STACK);
  /** Movement speed in water */
  protected static final EntityDataAccessor<Float> WATER_INERTIA = SynchedEntityData.defineId(ThrownTool.class, EntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Byte> TCONSTRUCT_LOYALTY = SynchedEntityData.defineId(ThrownTool.class, EntityDataSerializers.BYTE);
  private static final EntityDataAccessor<Boolean> TCONSTRUCT_FOIL = SynchedEntityData.defineId(ThrownTool.class, EntityDataSerializers.BOOLEAN);
  /** Volatile integer key for the loyalty level */
  public static final ResourceLocation LOYALTY = TConstruct.getResource("loyalty");
  /** Volatile integer key for the magnet level */
  public static final ResourceLocation MAGNET = TConstruct.getResource("magnet");

  @Nullable
  private IToolStackView tool = null;
  private float charge = 1;
  private float multiplier = 1;
  private boolean noDespawn = false;
  private boolean tconstructDealtDamage = false;
  private int tconstructDespawnLife = 0;
  private int magnet = 0;
  @Setter
  private int originalSlot = -1;
  private boolean hitBlock = false;
  /** Tasks queued by modifiers */
  private Schedule tasks = Schedule.EMPTY;

  public ThrownTool(EntityType<? extends ThrownTrident> type, Level level) {
    super(type, level);
  }

  public ThrownTool(Level level, LivingEntity shooter, ItemStack stack, float charge, float multiplier, float waterInertia) {
    this(TinkerTools.thrownTool.get(), level);
    // AbstractArrow - positional constructor
    this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    // AbstractArrow - shooter constructor
    this.setOwner(shooter);
    if (shooter instanceof Player) {
      this.pickup = AbstractArrow.Pickup.ALLOWED;
    }
    // trident - stack constructor
    this.setPickupItemStack(stack.copyWithCount(1));
    this.charge = charge;
    this.multiplier = multiplier;
    this.entityData.set(WATER_INERTIA, waterInertia);
    updateFromStack();
  }

  /** Sets any relevant properties from the stack */
  private void updateFromStack() {
    ItemStack tridentItem = getPickupItemStackOrigin();
    this.entityData.set(STACK, tridentItem);
    this.entityData.set(TCONSTRUCT_LOYALTY, (byte) ModifierUtil.getVolatileInt(tridentItem, LOYALTY));
    this.entityData.set(TCONSTRUCT_FOIL, ModifierUtil.checkVolatileFlag(tridentItem, ModifiableItem.SHINY));
    this.noDespawn = ModifierUtil.checkVolatileFlag(tridentItem, IndestructibleItemEntity.INDESTRUCTIBLE_ENTITY);
    if (!level().isClientSide) {
      this.magnet = ModifierUtil.getVolatileInt(tridentItem, MAGNET);
    }
  }

  /** Called after {@link #shoot(double, double, double, float, float)} but before the first tick of hte projectile to do final setup. */
  public void onRelease(LivingEntity entity, ModDataNBT arrowData) {
    IToolStackView tool = getTool();
    ItemStack tridentItem = getPickupItemStackOrigin();
    for (ModifierEntry entry : tool.getModifierList()) {
      entry.getHook(ModifierHooks.PROJECTILE_THROWN).onProjectileShoot(tool, entry, entity, tridentItem, this, null, arrowData, true);
    }
    this.tasks = ScheduledProjectileTaskModifierHook.createSchedule(tool, tridentItem, this, null, arrowData);
  }

  @Override
  protected float getWaterInertia() {
    return entityData.get(WATER_INERTIA);
  }

  public boolean isChanneling() {
    ItemStack tridentItem = getPickupItemStackOrigin();
    return !tridentItem.isEmpty() && getTool().getModifiers().getLevel(ModifierIds.channeling) > 0;
  }

  @Override
  public Component getDisplayName() {
    ItemStack tridentItem = getPickupItemStackOrigin();
    if (tridentItem.isEmpty()) {
      return super.getDisplayName();
    }
    IToolStackView tool = getTool();
    return ToolNameHook.getName(tool.getDefinition(), tridentItem, tool);
  }


  /* Despawn */

  @Override
  public void tickDespawn() {
    // if no pickup, despawn in 1 minute
    ItemStack tridentItem = getPickupItemStackOrigin();
    if (pickup != Pickup.ALLOWED || tridentItem.isEmpty()) {
      tconstructDespawnLife += 1;
      if (tconstructDespawnLife >= 1200) {
        this.discard();
      }
      // if its worldbound or loyalty, don't despawn
    } else if (!noDespawn && this.entityData.get(TCONSTRUCT_LOYALTY) == 0) {
      // otherwise despawn in 5 minutes like a normal item. Like seriously mojang, why does your rare enchanted trident despawn in 1 minute?
      this.tconstructDespawnLife += 1;
      if (this.tconstructDespawnLife >= 6000) {
        this.discard();
      }
    }
  }

  @Override
  protected void onBelowWorld() {
    // don't discard tools below world if they have loyalty
    if (pickup == Pickup.ALLOWED && this.entityData.get(TCONSTRUCT_LOYALTY) != 0) {
      // ensure it returns
      tconstructDealtDamage = true;
      // we don't damage the tool on throw, so instead damage it when it hits a block or an entity
      ItemStack tridentItem = getPickupItemStackOrigin();
      if (!tridentItem.isEmpty()) {
        ToolDamageUtil.damage(getTool(), 1, getOwner() instanceof LivingEntity l ? l : null, tridentItem);
      }
    } else {
      super.onBelowWorld();
    }
  }


  /* Combat */

  /** Gets the tool instance, ensuring its created */
  private IToolStackView getTool() {
    if (tool == null) {
      tool = ToolStack.from(getPickupItemStackOrigin());
    }
    return tool;
  }

  @Override
  public void tick() {
    // TODO: consider expiry time for loyalty
    ItemStack tridentItem = getPickupItemStackOrigin();
    if (!tconstructDealtDamage && inGroundTime > 4) {
      // we don't damage the tool on throw, so instead damage it when it hits a block or an entity
      if (!tridentItem.isEmpty() && !level().isClientSide) {
        ToolDamageUtil.damage(getTool(), 1, getOwner() instanceof LivingEntity l ? l : null, tridentItem);
        // update the stack so visual changes to the tool render (e.g. broken or fluid)
        // need to force since its the same instance, just NBT changes
        this.entityData.set(STACK, tridentItem, true);
      }
        tconstructDealtDamage = true;
    }

    // Vanilla trident return logic checks its private loyalty data accessor, so TConstruct loyalty
    // needs its own return path based on the synced modifier-derived value.
    int loyalty = this.entityData.get(TCONSTRUCT_LOYALTY);
    Entity owner = this.getOwner();
    if (loyalty > 0 && (tconstructDealtDamage || this.isNoPhysics()) && owner != null) {
      if (!isAcceptableReturnOwner(owner)) {
        if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
          this.spawnAtLocation(this.getPickupItem(), 0.1F);
        }
        this.discard();
      } else {
        this.setNoPhysics(true);
        Vec3 target = owner.getEyePosition().subtract(this.position());
        this.setPosRaw(this.getX(), this.getY() + target.y * 0.015 * (double)loyalty, this.getZ());
        if (this.level().isClientSide) {
          this.yOld = this.getY();
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(target.normalize().scale(0.05 * (double)loyalty)));
      }
    }
    super.tick();

    // magnet
    if (magnet > 0) {
      MagneticEffect.applyVelocity(level(), position(), magnet - 1, ItemEntity.class, 3, 0.05f, 32);
    }

    // check if any tasks are ready
    if (!tasks.isEmpty() && !tridentItem.isEmpty()) {
      ScheduledProjectileTaskModifierHook.checkSchedule(getTool(), tridentItem, this, null, tasks);
    }
  }

  /** Checks if the owner can receive a returning thrown tool. */
  private static boolean isAcceptableReturnOwner(Entity owner) {
    return owner.isAlive() && (!(owner instanceof ServerPlayer player) || !player.isSpectator());
  }

  @Override
  protected void onHitEntity(EntityHitResult pResult) {
    this.tconstructDealtDamage = true;

    // need a living entity to run our attack hooks, just do nothing if we lack an owner
    ItemStack tridentItem = getPickupItemStackOrigin();
    if (!tridentItem.isEmpty() && this.getOwner() instanceof LivingEntity owner) {
      Entity target = pResult.getEntity();

      IToolStackView tool = getTool();
      if (ToolAttackUtil.canPerformAttack(tool) && ToolAttackUtil.isAttackable(owner, target)) {
        // if the tool is blunted, don't deal damage and instead go squeak
        if (EntityInteractionModifierHook.meleeDisabled(tool)) {
          owner.playSound(Sounds.TOY_SQUEAK.getSound());
        } else {
          // hack: swap the offhand for the tool so any relevant modifier hooks (notably looting) see the right thing
          // does not actually matter which slot we use, just need the tool there to ensure hooks are properly run
          // skip the hack if attacking ourself, as that might cause it to drop/duplicate. Its not like we need looting on ourself, why are you killing yourself?
          ItemStack offhand = owner.getOffhandItem();
          boolean notSelf = owner != target;
          if (notSelf) {
            owner.setItemInHand(InteractionHand.OFF_HAND, tridentItem);
          }
          // TODO: consider whether redundant sound is fine
          ToolAttackContext context = ToolAttackContext.attacker(owner).target(target).hand(InteractionHand.OFF_HAND).baseDamage(tool.getStats().get(ToolStats.ATTACK_DAMAGE) * multiplier).cooldown(charge).projectile(this).build();
          if (ToolAttackUtil.performAttack(tool, context)) {
            if (target instanceof LivingEntity living) {
              this.doPostHurtEffects(living);
            }
          }

          // restore held item
          if (notSelf) {
            owner.setItemInHand(InteractionHand.OFF_HAND, offhand);
          }

          // cancel post hit logic if it hit an enderman with no enderference
          if (!TinkerEffects.canHitWithProjectile(context.getLivingTarget())) {
            return;
          }
        }
      }

      // back off from the target
      this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
      if (!level().isClientSide) {
        // play sound
        if (tool.getModifiers().getLevel(ModifierIds.channeling) == 0) {
          this.playSound(tool.isBroken() ? SoundEvents.ITEM_BREAK : SoundEvents.TRIDENT_HIT, 1.0f, 1.0f);
        }
        // update the stack so visual changes to the tool render (e.g. broken or fluid)
        // need to force since its the same instance, just NBT changes
        this.entityData.set(STACK, tridentItem, true);
      }
    }
  }


  /* block breaking */

  @Override
  protected void onHitBlock(BlockHitResult result) {
    // ensure we did not attempt to hit before
    if (!hitBlock) {
      // always mark as hit, don't want it deflecting off and hitting something else
      hitBlock = true;
      // skip if we hit a monster, also need a player as a lot of block breaking logic relies on players
      ItemStack tridentItem = getPickupItemStackOrigin();
      if (!tconstructDealtDamage && !tridentItem.isEmpty() && tridentItem.is(TinkerTags.Items.HARVEST) && this.getOwner() instanceof ServerPlayer owner) {
        // tool can't be broken; no running vanilla logic
        IToolStackView tool = getTool();
        if (!tool.isBroken()) {
          // must be effective and not unbreakable
          BlockPos pos = result.getBlockPos();
          Level level = level();
          BlockState state = level.getBlockState(pos);
          float hardness = state.getDestroySpeed(level, pos);
          if (hardness != -1 && IsEffectiveToolHook.isEffective(tool, state)) {
            // fetch base mining speed, though can skip if its already instant
            float miningSpeed = 1;
            if (hardness > 0) {
              miningSpeed = Math.max(1, tool.getHook(ToolHooks.MINING_SPEED).modifyDestroySpeed(tool, state, tool.getStats().get(ToolStats.MINING_SPEED)));
              float multiplier = charge * this.multiplier;
              // if underwater and no fins, give underwater penalty
              if (isInWater() && getWaterInertia() < 0.9f) {
                multiplier /= 5;
              }
              miningSpeed *= multiplier;

              // apply mining speed modifiers
              ModifierNBT modifiers = tool.getModifiers();
              Direction sideHit = result.getDirection();
              if (!modifiers.isEmpty()) {
                BreakSpeedContext context = new BreakSpeedContext.Direct(owner, state, pos, sideHit, true, miningSpeed, multiplier);
                for (ModifierEntry entry : tool.getModifiers()) {
                  miningSpeed = entry.getHook(ModifierHooks.BREAK_SPEED).modifyBreakSpeed(tool, entry, context, miningSpeed);
                }
              }
            }
            // normally, mining speed is added once per tick, and once it exceeds hardness * 30 the block breaks
            // for thrown tools, our condition is anything that breaks in 1 second, hence the factor of 1.5 * hardness
            if (miningSpeed > 1.5 * hardness) {
              // hack: swap the mainhand for the tool so relevant modifier hooks (notably loot tables) run correctly
              ItemStack mainhand = owner.getMainHandItem();
              owner.setItemInHand(InteractionHand.MAIN_HAND, tridentItem);
              int harvested = ToolHarvestLogic.runBlockBreak(tridentItem, tool, state, pos, result.getDirection(), owner, this);
              owner.setItemInHand(InteractionHand.MAIN_HAND, mainhand);

              // if we broke anything, back off and skip standard stick in block logic
              if (harvested > 0) {
                // no damaging a monster after this, and also reminds loyalty to return
                tconstructDealtDamage = true;
                // backing off the block makes the tool easier to collect
                this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
                // update the stack so visual changes to the tool render (e.g. broken or fluid)
                // need to force since its the same instance, just NBT changes
                if (!level.isClientSide) {
                  this.entityData.set(STACK, tridentItem, true);
                }
                return;
              }
            }
          }
        }
      }
    }
    super.onHitBlock(result);
  }


  /* returning to slot */

  /**
   * Handles returning the item to the player.
   * Unlike {@link Inventory#add(ItemStack)}, supports adding to the offhand/armor slots, and does not overwrite existing tool stacks in the slot.
   */
  private boolean addToInventory(Player player) {
    ItemStack pickup = getPickupItem();
    Inventory inventory = player.getInventory();
    if (originalSlot != -1) {
      ItemStack current = inventory.getItem(originalSlot);
      if (current.isEmpty()) {
        inventory.setItem(originalSlot, pickup);
        return true;
      } else if (current.getCount() < current.getMaxStackSize() && ItemStack.isSameItemSameComponents(current, pickup)) {
        current.grow(1);
        return true;
      }
    }
    return inventory.add(pickup);
  }

  @Override
  protected boolean tryPickup(Player player) {
    return switch (this.pickup) {
      case ALLOWED -> addToInventory(player);
      case CREATIVE_ONLY -> player.getAbilities().instabuild;
      default -> this.isNoPhysics() && this.ownedBy(player) && addToInventory(player);
    };
  }


  /* Client */

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(STACK, ItemStack.EMPTY);
    builder.define(WATER_INERTIA, 0.6f);
    builder.define(TCONSTRUCT_LOYALTY, (byte) 0);
    builder.define(TCONSTRUCT_FOIL, false);
  }

  @Override
  public ItemStack getDisplayTool() {
    return this.entityData.get(STACK);
  }


  /* NBT */
  private static final String KEY_CHARGE = "charge";
  private static final String KEY_MULTIPLIER = "multiplier";
  private static final String KEY_WATER_INERTIA = "water_inertia";
  private static final String KEY_ORIGINAL_SLOT = "original_slot";
  private static final String KEY_HIT_BLOCK = "hit_block";
  private static final String KEY_TASKS = "tasks";

  @Override
  public void addAdditionalSaveData(CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    tag.putFloat(KEY_CHARGE, this.charge);
    tag.putFloat(KEY_MULTIPLIER, this.multiplier);
    tag.putFloat(KEY_WATER_INERTIA, this.entityData.get(WATER_INERTIA));
    tag.putBoolean(KEY_HIT_BLOCK, hitBlock);
    if (this.originalSlot != -1) {
      tag.putInt(KEY_ORIGINAL_SLOT, this.originalSlot);
    }
    if (!this.tasks.isEmpty()) {
      tag.put(KEY_TASKS, this.tasks.serialize());
    }
  }

  @Override
  public void readAdditionalSaveData(CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    // update the tool to sync to client, if its set
    if (tag.contains("item", CompoundTag.TAG_COMPOUND)) {
      updateFromStack();
    }
    this.charge = tag.getFloat(KEY_CHARGE);
    this.multiplier = tag.getFloat(KEY_MULTIPLIER);
    this.entityData.set(WATER_INERTIA, tag.getFloat(KEY_WATER_INERTIA));
    this.hitBlock = tag.getBoolean(KEY_HIT_BLOCK);
    if (tag.contains(KEY_ORIGINAL_SLOT, Tag.TAG_ANY_NUMERIC)) {
      this.originalSlot = tag.getInt(KEY_ORIGINAL_SLOT);
    } else {
      this.originalSlot = -1;
    }
    if (tag.contains(KEY_TASKS, CompoundTag.TAG_LIST)) {
      this.tasks = Schedule.deserialize(tag.getList(KEY_TASKS, CompoundTag.TAG_COMPOUND));
    }
  }
}
