package slimeknights.mantle.loot.legacy;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

/** Compatibility serializer interface for legacy loot/condition JSON helpers. */
public interface LegacyLootSerializer<T> {
  void serialize(JsonObject json, T value, JsonSerializationContext context);

  T deserialize(JsonObject json, JsonDeserializationContext context);
}
