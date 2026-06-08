package slimeknights.tconstruct.library.tools.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.utils.ResourceId;

import java.util.function.BiFunction;

/**
 * Read only view of {@link ModDataNBT}
 */
public interface IModDataView {
  /** Empty variant of tool data */
  IModDataView EMPTY = new IModDataView() {
    @Override
    public <T> T get(ResourceLocation name, BiFunction<CompoundTag,String,T> function) {
      return function.apply(new CompoundTag(), name.toString());
    }

    @Override
    public boolean contains(ResourceLocation name) {
      return false;
    }

    @Override
    public boolean contains(ResourceLocation name, int type) {
      return false;
    }
  };

  /**
   * Gets a namespaced key from NBT
   * @param name      Namedspaced key
   * @param function  Function to get data using the key
   * @param <T>  NBT type of output
   * @return  Data based on the function
   */
  <T> T get(ResourceLocation name, BiFunction<CompoundTag,String,T> function);

  /**
   * Checks if the data contains the given tag with any type.
   * Generally, its better to use {@link #contains(ResourceLocation, int)}, but there are rare benefits to this method.
   * @param name  Namespaced key
   * @return  True if the tag is contained
   */
  boolean contains(ResourceLocation name);

  default boolean contains(ResourceId name) {
    return contains(name.getLocation());
  }

  /**
   * Checks if the data contains the given tag
   * @param name  Namespaced key
   * @param type  Tag type, see {@link Tag} for values
   * @return  True if the tag is contained
   */
  boolean contains(ResourceLocation name, int type);

  default boolean contains(ResourceId name, int type) {
    return contains(name.getLocation(), type);
  }

  /**
   * Gets the number of slots provided by this data. Will be 0 if this data does not support slots.
   * @param type  Type of slot to get
   * @return  Number of slots
   */
  default int getSlots(SlotType type) {
    return 0;
  }


  /* Helpers */

  /**
   * Reads an generic NBT value from the mod data
   * @param name  Name
   * @return  Integer value
   */
  default Tag get(ResourceLocation name) {
    return get(name, CompoundTag::get);
  }

  /** Reads a generic NBT value from the mod data using a typed TConstruct ID. */
  default Tag get(ResourceId name) {
    return get(name.getLocation());
  }

  /**
   * Reads an integer from the mod data
   * @param name  Name
   * @return  Integer value
   */
  default int getInt(ResourceLocation name) {
    return get(name, CompoundTag::getInt);
  }

  default int getInt(ResourceId name) {
    return getInt(name.getLocation());
  }

  /**
   * Reads an boolean from the mod data
   * @param name  Name
   * @return  Boolean value
   */
  default boolean getBoolean(ResourceLocation name) {
    return get(name, CompoundTag::getBoolean);
  }

  default boolean getBoolean(ResourceId name) {
    return getBoolean(name.getLocation());
  }

  /**
   * Reads an float from the mod data
   * @param name  Name
   * @return  Float value
   */
  default float getFloat(ResourceLocation name) {
    return get(name, CompoundTag::getFloat);
  }

  default float getFloat(ResourceId name) {
    return getFloat(name.getLocation());
  }

  /**
   * Reads a string from the mod data
   * @param name  Name
   * @return  String value
   */
  default String getString(ResourceLocation name) {
    return get(name, CompoundTag::getString);
  }

  default String getString(ResourceId name) {
    return getString(name.getLocation());
  }

  /**
   * Reads a compound from the mod data
   * @param name  Name
   * @return  Compound value
   */
  default CompoundTag getCompound(ResourceLocation name) {
    return get(name, CompoundTag::getCompound);
  }

  default CompoundTag getCompound(ResourceId name) {
    return getCompound(name.getLocation());
  }

  /**
   * Reads a list from the mod data
   * @param name  Name
   * @param type  List type
   * @return  List value
   */
  default ListTag getList(ResourceLocation name, int type) {
    return get(name, (tag, key) -> tag.getList(key, type));
  }

  default ListTag getList(ResourceId name, int type) {
    return getList(name.getLocation(), type);
  }
}
