package slimeknights.mantle.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class TierSortingRegistry {
  private TierSortingRegistry() {}

  public static List<Tier> getSortedTiers() {
    return Arrays.asList(Tiers.values());
  }

  @Nullable
  public static Tier byName(ResourceLocation id) {
    if (!ResourceLocation.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
      return null;
    }
    for (Tiers tier : Tiers.values()) {
      if (tier.name().equalsIgnoreCase(id.getPath())) {
        return tier;
      }
    }
    return null;
  }

  public static boolean isCorrectTierForDrops(Tier tier, BlockState state) {
    return !state.is(tier.getIncorrectBlocksForDrops());
  }

  @Nullable
  public static ResourceLocation getName(Tier tier) {
    if (tier instanceof Tiers vanilla) {
      return ResourceLocation.withDefaultNamespace(vanilla.name().toLowerCase(Locale.ROOT));
    }
    return null;
  }
}
