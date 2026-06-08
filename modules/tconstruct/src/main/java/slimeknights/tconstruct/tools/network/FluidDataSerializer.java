package slimeknights.tconstruct.tools.network;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

/** Serializer for fluid stack data in entities */
public class FluidDataSerializer {
  public static final EntityDataSerializer<FluidStack> SERIALIZER = EntityDataSerializer.forValueType(FluidStack.OPTIONAL_STREAM_CODEC);
}
