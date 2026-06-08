package slimeknights.mantle.recipe.ingredient.compat;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;

public enum VanillaIngredientSerializer implements IIngredientSerializer<Ingredient> {
  INSTANCE;

  @Override
  public Ingredient parse(FriendlyByteBuf buffer) {
    return Ingredient.CONTENTS_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf) buffer);
  }

  @Override
  public Ingredient parse(JsonObject json) {
    return Ingredient.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json).result().orElse(Ingredient.EMPTY);
  }

  @Override
  public void write(FriendlyByteBuf buffer, Ingredient ingredient) {
    Ingredient.CONTENTS_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf) buffer, ingredient);
  }
}
