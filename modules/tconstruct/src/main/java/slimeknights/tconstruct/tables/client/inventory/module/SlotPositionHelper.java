package slimeknights.tconstruct.tables.client.inventory.module;

import net.minecraft.world.inventory.Slot;

import java.lang.reflect.Field;

/** Shared helper for client screens that need to move menu slots after layout changes. */
public final class SlotPositionHelper {
  private static final Field SLOT_X_FIELD = slotField("x");
  private static final Field SLOT_Y_FIELD = slotField("y");

  private SlotPositionHelper() {}

  public static void setSlotPosition(Slot slot, int x, int y) {
    try {
      SLOT_X_FIELD.setInt(slot, x);
      SLOT_Y_FIELD.setInt(slot, y);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unable to update slot position", e);
    }
  }

  private static Field slotField(String name) {
    try {
      Field field = Slot.class.getDeclaredField(name);
      field.setAccessible(true);
      return field;
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }
}
