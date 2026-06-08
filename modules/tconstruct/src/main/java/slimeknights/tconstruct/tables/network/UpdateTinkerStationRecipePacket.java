package slimeknights.tconstruct.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;

import java.util.Optional;

/**
 * Packet to send the current crafting recipe to a player who opens the tinker station
 */
public class UpdateTinkerStationRecipePacket implements IThreadsafePacket {
  public static final CustomPacketPayload.Type<UpdateTinkerStationRecipePacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("update_tinker_station_recipe"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTinkerStationRecipePacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    UpdateTinkerStationRecipePacket::new
  );

  private final BlockPos pos;
  private final ResourceLocation recipe;
  public UpdateTinkerStationRecipePacket(BlockPos pos, ITinkerStationRecipe recipe) {
    this.pos = pos;
    // TODO 1.21: pass RecipeHolder ids through the tinker station cache.
    this.recipe = TConstruct.getResource("unknown_tinker_station_recipe");
  }

  public UpdateTinkerStationRecipePacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.recipe = buffer.readResourceLocation();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeResourceLocation(recipe);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(UpdateTinkerStationRecipePacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        Optional<ITinkerStationRecipe> recipe = RecipeHelper.getRecipe(world.getRecipeManager(), packet.recipe, ITinkerStationRecipe.class);

        recipe.ifPresent(r -> BlockEntityHelper.get(TinkerStationBlockEntity.class, world, packet.pos).ifPresent(te -> te.updateRecipe(r)));
      }
    }
  }
}
