package slimeknights.tconstruct.library.modifiers.modules.build;

import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

import java.util.List;

public final class SwappableSlotModule implements VolatileDataModifierHook, ModifierModule, ConditionalModule<IToolContext> {
  public static final String FORMAT = TConstruct.makeTranslationKey("modifier", "swappable_slot.format");
  public static final RecordLoadable<SwappableSlotModule> LOADER = new SingletonLoader<>(loader -> new SwappableSlotModule(1, ModifierCondition.ANY_CONTEXT));
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<SwappableSlotModule>defaultHooks(ModifierHooks.VOLATILE_DATA);

  private final int slots;
  private final ModifierCondition<IToolContext> condition;

  public SwappableSlotModule(int slots) {
    this(slots, ModifierCondition.ANY_CONTEXT);
  }

  public SwappableSlotModule(int slots, ModifierCondition<IToolContext> condition) {
    this.slots = slots;
    this.condition = condition;
  }

  public SwappableSlotModule(SlotType type, int slots, ModifierCondition<IToolContext> condition) {
    this(slots, condition);
  }

  @Override
  public ModifierCondition<IToolContext> condition() {
    return condition;
  }

  @Override
  public RecordLoadable<SwappableSlotModule> getLoader() { return LOADER; }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() { return DEFAULT_HOOKS; }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    if (condition.matches(context, modifier)) {
      volatileData.addSlots(SlotType.UPGRADE, slots);
    }
  }

  public static final class BonusSlot implements VolatileDataModifierHook, ModifierModule, ConditionalModule<IToolContext> {
    public static final RecordLoadable<BonusSlot> LOADER = new SingletonLoader<>(loader -> new BonusSlot(SlotType.ABILITY, SlotType.UPGRADE, -1, ModifierCondition.ANY_CONTEXT));
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<BonusSlot>defaultHooks(ModifierHooks.VOLATILE_DATA);

    private final SlotType from;
    private final SlotType to;
    private final int amount;
    private final ModifierCondition<IToolContext> condition;

    public BonusSlot(SlotType from, SlotType to, int amount, ModifierCondition<IToolContext> condition) {
      this.from = from;
      this.to = to;
      this.amount = amount;
      this.condition = condition;
    }

    @Override
    public ModifierCondition<IToolContext> condition() {
      return condition;
    }

    @Override
    public RecordLoadable<BonusSlot> getLoader() { return LOADER; }

    @Override
    public List<ModuleHook<?>> getDefaultHooks() { return DEFAULT_HOOKS; }

    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
      if (condition.matches(context, modifier)) {
        volatileData.addSlots(to, amount);
      }
    }
  }
}
