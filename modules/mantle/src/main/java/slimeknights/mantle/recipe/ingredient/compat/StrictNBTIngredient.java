package slimeknights.mantle.recipe.ingredient.compat;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public class StrictNBTIngredient extends AbstractIngredient {
  public StrictNBTIngredient(ItemStack stack) {
    super(Stream.of(new ItemValue(stack)));
  }

  public enum Serializer implements IIngredientSerializer<StrictNBTIngredient> {
    INSTANCE;

    @Override
    public StrictNBTIngredient parse(FriendlyByteBuf buffer) {
      return new StrictNBTIngredient(ItemStack.OPTIONAL_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf) buffer));
    }

    @Override
    public StrictNBTIngredient parse(JsonObject json) {
      return new StrictNBTIngredient(ItemStack.EMPTY);
    }

    @Override
    public void write(FriendlyByteBuf buffer, StrictNBTIngredient ingredient) {}
  }
}
