package slimeknights.tconstruct.library.tools.item.armor;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.registration.object.IdAwareObject;

import java.util.List;
import java.util.Map;

/** Armor material wrapper that returns 0 stats except texture/sound, since we bypass all other usages */
@Getter
public class DummyArmorMaterial implements IdAwareObject {
  private final ResourceLocation id;
  private final SoundEvent equipSound;
  private final Holder<ArmorMaterial> holder;

  public DummyArmorMaterial(ResourceLocation id, SoundEvent equipSound) {
    this.id = id;
    this.equipSound = equipSound;
    this.holder = Holder.direct(createMaterial(id, equipSound));
  }

  private static ArmorMaterial createMaterial(ResourceLocation id, SoundEvent equipSound) {
    return new ArmorMaterial(
      Map.of(),
      0,
      BuiltInRegistries.SOUND_EVENT.wrapAsHolder(equipSound),
      () -> Ingredient.EMPTY,
      List.of(new ArmorMaterial.Layer(id)),
      0,
      0
    );
  }
}
