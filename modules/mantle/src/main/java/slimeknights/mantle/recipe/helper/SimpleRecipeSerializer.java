package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.Mantle;

import java.util.function.Function;

/** Simple implementation of a recipe serializer with no properties other than recipe ID. */
public record SimpleRecipeSerializer<T extends Recipe<?>>(Function<ResourceLocation,T> constructor) implements RecipeSerializer<T> {
  public T fromJson(ResourceLocation id, JsonObject pSerializedRecipe) {
    return constructor.apply(id);
  }

  public T fromNetwork(ResourceLocation id, FriendlyByteBuf pBuffer) {
    return constructor.apply(id);
  }

  public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {}

  @Override
  public MapCodec<T> codec() {
    return MapCodec.unit(() -> constructor.apply(Mantle.getResource("simple")));
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
    return StreamCodec.of((buffer, recipe) -> {}, buffer -> constructor.apply(Mantle.getResource("network")));
  }
}
