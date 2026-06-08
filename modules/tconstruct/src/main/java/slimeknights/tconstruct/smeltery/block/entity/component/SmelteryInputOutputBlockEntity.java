package slimeknights.tconstruct.smeltery.block.entity.component;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.inventory.EmptyItemHandler;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.common.multiblock.IMasterLogic;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static slimeknights.mantle.util.RetexturedHelper.TAG_TEXTURE;

/**
 * Shared logic between drains and ducts
 */
public abstract class SmelteryInputOutputBlockEntity<T> extends SmelteryComponentBlockEntity implements IRetexturedBlockEntity {
  /** Empty capability for in case the valid capability becomes invalid without invalidating */
  protected final T emptyInstance;
  @Nullable
  private T capabilityHolder = null;

  /* Retexturing */
  @Nonnull
  @Getter
  private Block texture = Blocks.AIR;

  protected SmelteryInputOutputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, T emptyInstance) {
    super(type, pos, state);
    this.emptyInstance = emptyInstance;
  }

  /** Clears all cached capabilities */
  private void clearHandler() {
    capabilityHolder = null;
  }

  @Override
  public void onMasterLoad(IMasterLogic master) {
    clearHandler();
  }

  @Override
  protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
    assert level != null;

    boolean masterChanged = false;
    if (!Objects.equals(getMasterPos(), master)) {
      clearHandler();
      masterChanged = true;
    }
    super.setMaster(master, block);
    if (masterChanged) {
      level.blockUpdated(worldPosition, getBlockState().getBlock());
      level.invalidateCapabilities(worldPosition);
    }
  }

  /**
   * Gets the capability to store in this IO block
   * @param parent  Parent tile entity
   * @return  Capability from parent, or null if absent
   */
  @Nullable
  protected abstract T fetchCapability(BlockEntity parent);

  /** Fetches the capability handlers if missing */
  @Nullable
  protected T getCachedCapability() {
    if (capabilityHolder == null && validateMaster()) {
      BlockPos master = getMasterPos();
      if (master != null && level != null) {
        BlockEntity te = level.getBlockEntity(master);
        if (te != null) {
          capabilityHolder = fetchCapability(te);
        }
      }
    }
    return capabilityHolder;
  }


  /* Retexturing */

  @Override
  @Nonnull
  public ModelData getModelData() {
    return RetexturedHelper.getModelData(getTexture());
  }

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(texture);
  }

  @Override
  public void updateTexture(String name) {
    Block oldTexture = texture;
    texture = RetexturedHelper.getBlock(name);
    if (oldTexture != texture) {
      setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }


  /* NBT */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(CompoundTag tags) {
    super.saveSynced(tags);
    if (texture != Blocks.AIR) {
      tags.putString(TAG_TEXTURE, getTextureName());
    }
  }

  @Override
  protected void loadAdditional(CompoundTag tags, HolderLookup.Provider registries) {
    super.loadAdditional(tags, registries);
    if (tags.contains(TAG_TEXTURE, Tag.TAG_STRING)) {
      texture = RetexturedHelper.getBlock(tags.getString(TAG_TEXTURE));
      RetexturedHelper.onTextureUpdated(this);
    }
  }


  /** Fluid implementation of smeltery IO */
  public static abstract class SmelteryFluidIO extends SmelteryInputOutputBlockEntity<IFluidHandler> {
    protected SmelteryFluidIO(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state, EmptyFluidHandler.INSTANCE);
    }

    /** Wraps the given capability */
    @Nullable
    protected IFluidHandler wrapCapability(@Nullable IFluidHandler capability) {
      return capability;
    }

    @Override
    @Nullable
    protected IFluidHandler fetchCapability(BlockEntity parent) {
      if (parent instanceof ISmelteryTankHandler tankHandler) {
        return wrapCapability(tankHandler.getFluidCapability());
      }
      return null;
    }

    /** Gets the fluid handler exposed by this IO block */
    @Nullable
    public IFluidHandler getFluidHandler() {
      return getCachedCapability();
    }
  }

  /** Item implementation of smeltery IO */
  public static class ChuteBlockEntity extends SmelteryInputOutputBlockEntity<IItemHandler> {
    public ChuteBlockEntity(BlockPos pos, BlockState state) {
      this(TinkerSmeltery.chute.get(), pos, state);
    }

    protected ChuteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state, EmptyItemHandler.INSTANCE);
    }

    @Override
    @Nullable
    protected IItemHandler fetchCapability(BlockEntity parent) {
      if (level == null) {
        return null;
      }
      BlockPos pos = parent.getBlockPos();
      return level.getCapability(
        net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
        pos, parent.getBlockState(), parent, null
      );
    }

    /** Gets the item handler exposed by this IO block */
    @Nullable
    public IItemHandler getItemHandler() {
      return getCachedCapability();
    }
  }
}
