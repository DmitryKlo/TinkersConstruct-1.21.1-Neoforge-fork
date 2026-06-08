package slimeknights.tconstruct.world.worldgen.trees;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.block.grower.TreeGrower;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.block.FoliageType;

import java.util.Optional;

/** Factory for slime tree {@link TreeGrower} instances. TreeGrower is final in 1.21, so use composition. */
public final class SlimeTree {
  private SlimeTree() {}

  public static TreeGrower create(FoliageType foliageType) {
    return switch (foliageType) {
      case EARTH -> grower("earth_slime", 0, TinkerStructures.earthSlimeTree);
      case SKY -> grower("sky_slime", 0, TinkerStructures.skySlimeTree);
      case ENDER -> grower("ender_slime", 0.15f, TinkerStructures.enderSlimeTreeTall, TinkerStructures.enderSlimeTree);
      case BLOOD -> grower("blood_slime", 0, TinkerStructures.bloodSlimeFungus);
      case ICHOR -> grower("ichor_slime", 0, TinkerStructures.ichorSlimeFungus);
    };
  }

  private static TreeGrower grower(String name, float secondaryChance, ResourceKey<ConfiguredFeature<?, ?>> tree) {
    return grower(name, secondaryChance, tree, null);
  }

  private static TreeGrower grower(String name, float secondaryChance, ResourceKey<ConfiguredFeature<?, ?>> tree, ResourceKey<ConfiguredFeature<?, ?>> secondaryTree) {
    return new TreeGrower(
      name,
      secondaryChance,
      Optional.empty(),
      Optional.empty(),
      Optional.of(tree),
      secondaryTree != null ? Optional.of(secondaryTree) : Optional.empty(),
      Optional.empty(),
      Optional.empty()
    );
  }
}
