package slimeknights.tconstruct.library.json.predicate.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags.Items;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** Item sub-predicate for matching Tinker tools using {@link ToolStackPredicate} */
public record ToolStackItemPredicate(IJsonPredicate<IToolStackView> predicate) implements ItemSubPredicate {
  public static final ResourceLocation ID = TConstruct.getResource("tool_stack");
  private static ItemSubPredicate.Type TYPE;

  public static final Codec<ToolStackItemPredicate> CODEC = Codec.unit(new ToolStackItemPredicate(ToolStackPredicate.ANY));

  /** Registers the sub-predicate type with vanilla */
  public static void register() {
    // ItemSubPredicates registration is private in 1.21. Keep the hook as a no-op until this predicate is properly ported.
  }

  private static ItemSubPredicate.Type getType() {
    if (TYPE == null) {
      register();
    }
    return TYPE;
  }

  /** Creates an item predicate matching the given tool stack predicate */
  public static ItemPredicate ofTool(IJsonPredicate<IToolStackView> predicate) {
    return ItemPredicate.Builder.item().withSubPredicate(getType(), new ToolStackItemPredicate(predicate)).build();
  }

  /** Creates an item predicate matching the given tool context predicate */
  public static ItemPredicate ofContext(IJsonPredicate<IToolContext> predicate) {
    return ofTool(ToolStackPredicate.context(predicate));
  }

  @Override
  public boolean matches(ItemStack stack) {
    // tag check is important to prevent accidently modifying the NBT of non-tools
    return stack.is(Items.MODIFIABLE) && predicate.matches(ToolStack.from(stack));
  }

  /** Deserializes the tool predicate from JSON */
  public static ToolStackItemPredicate deserialize(JsonObject json) {
    return new ToolStackItemPredicate(ToolStackPredicate.LOADER.getIfPresent(json, "predicate"));
  }
}
