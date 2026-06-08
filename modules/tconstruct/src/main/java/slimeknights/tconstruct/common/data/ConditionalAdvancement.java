package slimeknights.tconstruct.common.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.util.CraftingHelper;

import java.util.ArrayList;
import java.util.List;

/** Shim for removed NeoForge conditional advancement datagen. */
public final class ConditionalAdvancement {
  private ConditionalAdvancement() {}

  public static class Builder {
    private static final ResourceLocation PLACEHOLDER = ResourceLocation.fromNamespaceAndPath("neoforge", "conditional");

    private final List<ICondition> conditions = new ArrayList<>();
    private Advancement.Builder advancement;

    public Builder addCondition(ICondition condition) {
      this.conditions.add(condition);
      return this;
    }

    public Builder addAdvancement(Advancement.Builder advancement) {
      this.advancement = advancement;
      return this;
    }

    public JsonObject write() {
      if (advancement == null) {
        throw new IllegalStateException("No advancement set on conditional advancement builder");
      }
      JsonObject json = Advancement.CODEC.encodeStart(JsonOps.INSTANCE, advancement.build(PLACEHOLDER).value())
        .getOrThrow(IllegalStateException::new)
        .getAsJsonObject();
      if (!conditions.isEmpty()) {
        json.add("neoforge:conditions", CraftingHelper.serialize(conditions.toArray(ICondition[]::new)));
      }
      return json;
    }
  }
}
