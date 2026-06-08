package slimeknights.tconstruct.library.tools.definition;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;

import java.util.Map;
import java.util.Map.Entry;

/** Packet to sync tool definitions to the client */
@RequiredArgsConstructor
public class UpdateToolDefinitionDataPacket implements IThreadsafePacket {
  public static final CustomPacketPayload.Type<UpdateToolDefinitionDataPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("update_tool_definition_data"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateToolDefinitionDataPacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.write(buffer), UpdateToolDefinitionDataPacket::new);

  @Getter(AccessLevel.PROTECTED)
  private final Map<ResourceLocation, ToolDefinitionData> dataMap;

  public UpdateToolDefinitionDataPacket(RegistryFriendlyByteBuf buffer) {
    int size = buffer.readVarInt();
    ImmutableMap.Builder<ResourceLocation, ToolDefinitionData> builder = ImmutableMap.builder();
    for (int i = 0; i < size; i++) {
      ResourceLocation name = buffer.readResourceLocation();
      ToolDefinitionData data = ToolDefinitionData.LOADABLE.decode(buffer, ToolDefinitionLoader.contextBuilder(name).build());
      builder.put(name, data);
    }
    dataMap = builder.build();
  }

  private void write(RegistryFriendlyByteBuf buffer) {
    buffer.writeVarInt(dataMap.size());
    for (Entry<ResourceLocation, ToolDefinitionData> entry : dataMap.entrySet()) {
      buffer.writeResourceLocation(entry.getKey());
      ToolDefinitionData.LOADABLE.encode(buffer, entry.getValue());
    }
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    ToolDefinitionLoader.getInstance().updateDataFromServer(dataMap);
  }
}
