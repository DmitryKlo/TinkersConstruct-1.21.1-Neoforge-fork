package slimeknights.mantle.recipe.ingredient.compat;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;

public interface IIngredientSerializer<T> {
  T parse(FriendlyByteBuf buffer);

  T parse(JsonObject json);

  void write(FriendlyByteBuf buffer, T ingredient);
}
