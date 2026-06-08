package slimeknights.mantle.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.common.FluidStackLoadable;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.util.FluidCapabilityHelper;

import java.util.List;

/**
 * Loot function to set the fluid on a dropped item
 */
public class SetFluidLootFunction extends LootItemConditionalFunction {
  public static final MapCodec<SetFluidLootFunction> CODEC = MapCodec.unit(() -> new SetFluidLootFunction(List.of(), FluidStack.EMPTY));
  public static final Serializer SERIALIZER = new Serializer();

  /** Fluid to add to the item */
  private final FluidStack fluid;
  protected SetFluidLootFunction(List<LootItemCondition> conditionsIn, FluidStack fluid) {
    super(conditionsIn);
    this.fluid = fluid;
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    return FluidCapabilityHelper.item(stack)
                .map(handler -> {
                  handler.fill(fluid.copy(), FluidAction.EXECUTE);
                  return handler.getContainer();
                }).orElse(stack);
  }

  @Override
  public LootItemFunctionType getType() {
    return MantleLoot.SET_FLUID_FUNCTION;
  }

  /**
   * Creates a new builder with the given fluid
   * @param fluid  Fluid to set
   * @return  Builder instance
   */
  public static Builder<?> builder(FluidStack fluid) {
    return simpleBuilder(conditions -> new SetFluidLootFunction(conditions, fluid));
  }

  /** Serializer logic for the function */
  private static class Serializer implements slimeknights.mantle.loot.legacy.LegacyLootSerializer<SetFluidLootFunction> {
    public void serialize(JsonObject json, SetFluidLootFunction loot, JsonSerializationContext context) {
      json.add("fluid", FluidStackLoadable.REQUIRED_STACK_NBT.serialize(loot.fluid));
    }

    @Override
    public SetFluidLootFunction deserialize(JsonObject object, JsonDeserializationContext context) {
      FluidStack fluid = FluidStackLoadable.REQUIRED_STACK_NBT.getIfPresent(object, "fluid");
      return new SetFluidLootFunction(List.of(), fluid);
    }
  }
}
