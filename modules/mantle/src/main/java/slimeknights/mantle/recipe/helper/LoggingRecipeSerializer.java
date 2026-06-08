package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.JsonCodec;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Recipe serializer that logs network exceptions before throwing them as otherwise the exceptions may be invisible
 * @param <T>  Recipe class
 */
public interface LoggingRecipeSerializer<T extends Recipe<?>> extends RecipeSerializer<T> {
  /** Wraps legacy JSON recipe parsing in the 1.21 codec entrypoint. */
  static <T extends Recipe<?>> MapCodec<T> codecFromJson(BiFunction<ResourceLocation, JsonObject, T> parser) {
    return MapCodec.assumeMapUnsafe(new JsonCodec<>() {
      @Override
      public T deserialize(JsonElement element, DynamicOps<?> ops) {
        return parser.apply(Mantle.getResource("codec"), GsonHelper.convertToJsonObject(element, "recipe"));
      }

      @Override
      public JsonElement serialize(T object, DynamicOps<?> ops) {
        return new JsonObject();
      }
    });
  }

  /**
   * Read the recipe from the packet
   * @param id      Recipe ID
   * @param buffer  Buffer instance
   * @return  Parsed recipe
   * @throws RuntimeException  If any errors happen, the exception will be logged automatically
   */
  @Nullable
  T fromNetworkSafe(ResourceLocation id, FriendlyByteBuf buffer);

  /**
   * Write the method to the buffer
   * @param buffer  Buffer instance
   * @param recipe  Recipe instance
   * @throws RuntimeException  If any errors happen, the exception will be logged automatically
   */
  void toNetworkSafe(FriendlyByteBuf buffer, T recipe);

  @Nullable
  default T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
    try {
      return fromNetworkSafe(id, buffer);
    } catch (RuntimeException e) {
      Mantle.logger.error("{}: Error reading recipe {} from packet", this.getClass().getSimpleName(), id, e);
      throw e;
    }
  }

  default void toNetwork(FriendlyByteBuf buffer, T recipe) {
    try {
      toNetworkSafe(buffer, recipe);
    } catch (RuntimeException e) {
      Mantle.logger.error("{}: Error writing recipe of class {} and type {} to packet", this.getClass().getSimpleName(), recipe.getClass().getSimpleName(), recipe.getType(), e);
      throw e;
    }
  }

  @Override
  default MapCodec<T> codec() {
    return MapCodec.unit((T) null);
  }

  @Override
  default StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
    return StreamCodec.of(
      (buffer, recipe) -> toNetwork(buffer, recipe),
      buffer -> fromNetwork(Mantle.getResource("network"), buffer));
  }
}
