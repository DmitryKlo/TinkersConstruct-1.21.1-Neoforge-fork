package slimeknights.tconstruct.library.json.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.shared.TinkerCommons;

import java.util.List;
import java.util.function.Consumer;

/** @deprecated use {@link slimeknights.mantle.loot.entry.TagPreferenceLootEntry} */
@Deprecated(forRemoval = true)
public class TagPreferenceLootEntry extends LootPoolSingletonContainer {
  public static final MapCodec<TagPreferenceLootEntry> CODEC = MapCodec.unit(() -> new TagPreferenceLootEntry(TagKey.create(Registries.ITEM, TConstruct.getResource("air")), 1, 0, List.of(), List.of()));
  private final TagKey<Item> tag;

  protected TagPreferenceLootEntry(TagKey<Item> tag, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
    super(weight, quality, conditions, functions);
    this.tag = tag;
  }

  @SuppressWarnings("removal")
  @Override
  public LootPoolEntryType getType() {
    return TinkerCommons.lootTagPreference.get();
  }

  @Override
  protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
    TagPreference.getPreference(tag).ifPresent(item -> consumer.accept(new ItemStack(item)));
  }

  /** @deprecated use {@link slimeknights.mantle.loot.entry.TagPreferenceLootEntry#tagPreference(TagKey)} */
  @Deprecated(forRemoval = true)
  public static LootPoolSingletonContainer.Builder<?> tagPreference(TagKey<Item> tag) {
    return slimeknights.mantle.loot.entry.TagPreferenceLootEntry.tagPreference(tag);
  }

  public static class Serializer implements slimeknights.mantle.loot.legacy.LegacyLootSerializer<TagPreferenceLootEntry> {
    public void serializeCustom(JsonObject json, TagPreferenceLootEntry object, JsonSerializationContext conditions) {
      json.addProperty("tag", object.tag.location().toString());
    }

    @Override
    public void serialize(JsonObject json, TagPreferenceLootEntry value, JsonSerializationContext context) {
      serializeCustom(json, value, context);
    }

    @Override
    public TagPreferenceLootEntry deserialize(JsonObject json, JsonDeserializationContext context) {
      TConstruct.LOG.warn("Using deprecated tag preference loot entry 'tconstruct:tag_preference', use 'mantle:tag_preference' instead");
      TagKey<Item> tag = TagKey.create(Registries.ITEM, JsonHelper.getResourceLocation(json, "tag"));
      return new TagPreferenceLootEntry(tag, 1, 0, List.of(), List.of());
    }
  }
}
