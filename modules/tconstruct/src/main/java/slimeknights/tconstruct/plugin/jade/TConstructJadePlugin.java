package slimeknights.tconstruct.plugin.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.AbstractCastingBlock;
import slimeknights.tconstruct.smeltery.block.CastingTankBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.controller.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.CastingTankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.AlloyerBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.MelterBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule.FuelInfo;
import slimeknights.tconstruct.smeltery.block.entity.tank.SmelteryTank;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

@WailaPlugin(TConstruct.MOD_ID)
public class TConstructJadePlugin implements IWailaPlugin {
  private static final ResourceLocation SMELTERY_INFO = TConstruct.getResource("smeltery_info");

  @Override
  public void register(IWailaCommonRegistration registration) {
    registration.registerBlockDataProvider(SmelteryInfoProvider.INSTANCE, HeatingStructureBlockEntity.class);
    registration.registerBlockDataProvider(SmelteryInfoProvider.INSTANCE, MelterBlockEntity.class);
    registration.registerBlockDataProvider(SmelteryInfoProvider.INSTANCE, AlloyerBlockEntity.class);
    registration.registerBlockDataProvider(SmelteryInfoProvider.INSTANCE, CastingBlockEntity.class);
    registration.registerBlockDataProvider(SmelteryInfoProvider.INSTANCE, CastingTankBlockEntity.class);
    registration.registerBlockDataProvider(SmelteryInfoProvider.INSTANCE, TankBlockEntity.class);
  }

  @Override
  public void registerClient(IWailaClientRegistration registration) {
    registration.registerBlockComponent(SmelteryInfoProvider.INSTANCE, ControllerBlock.class);
    registration.registerBlockComponent(SmelteryInfoProvider.INSTANCE, AbstractCastingBlock.class);
    registration.registerBlockComponent(SmelteryInfoProvider.INSTANCE, CastingTankBlock.class);
    registration.registerBlockComponent(SmelteryInfoProvider.INSTANCE, SearedTankBlock.class);
  }

  private enum SmelteryInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final String TAG_LINES = "tconstruct:jade_lines";

    @Override
    public ResourceLocation getUid() {
      return SMELTERY_INFO;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
      BlockEntity blockEntity = accessor.getBlockEntity();
      List<String> lines = new ArrayList<>();

      if (blockEntity instanceof HeatingStructureBlockEntity structure) {
        addSmelteryTank(lines, "Fluids", structure.getTank());
        addFuel(lines, structure.getFuelModule());
      } else if (blockEntity instanceof MelterBlockEntity melter) {
        addTank(lines, "Fluid", melter.getTank());
        addFuel(lines, melter.getFuelModule());
      } else if (blockEntity instanceof AlloyerBlockEntity alloyer) {
        addTank(lines, "Fluid", alloyer.getTank());
        addFuel(lines, alloyer.getFuelModule());
      } else if (blockEntity instanceof CastingBlockEntity casting) {
        addTank(lines, "Casting fluid", casting.getTank());
        addItem(lines, "Input", casting.getItem(CastingBlockEntity.INPUT));
        addItem(lines, "Output", casting.getItem(CastingBlockEntity.OUTPUT));
        if (casting.getCoolingTime() > 0) {
          lines.add("Casting: " + casting.getTimer() + "/" + casting.getCoolingTime() + " ticks");
        }
      } else if (blockEntity instanceof CastingTankBlockEntity castingTank) {
        addTank(lines, "Fluid", castingTank.getTank());
        addItem(lines, "Input", castingTank.getItem(CastingTankBlockEntity.INPUT));
        addItem(lines, "Output", castingTank.getItem(CastingTankBlockEntity.OUTPUT));
      } else if (blockEntity instanceof ITankBlockEntity tankBlockEntity) {
        addTank(lines, "Fluid", tankBlockEntity.getTank());
      }

      if (!lines.isEmpty()) {
        ListTag lineTags = new ListTag();
        for (String line : lines) {
          lineTags.add(StringTag.valueOf(line));
        }
        data.put(TAG_LINES, lineTags);
      }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
      if (!config.get(SMELTERY_INFO)) {
        return;
      }
      ListTag lineTags = accessor.getServerData().getList(TAG_LINES, Tag.TAG_STRING);
      for (int i = 0; i < lineTags.size(); i++) {
        tooltip.add(Component.literal(lineTags.getString(i)));
      }
    }

    private static void addSmelteryTank(List<String> lines, String label, SmelteryTank<?> tank) {
      if (tank.getContained() <= 0) {
        return;
      }
      lines.add(label + ": " + tank.getContained() + "/" + tank.getCapacity() + " mB");
      for (FluidStack fluid : tank.getFluids()) {
        addFluid(lines, " -", fluid, fluid.getAmount());
      }
    }

    private static void addTank(List<String> lines, String label, IFluidHandler tank) {
      for (int i = 0; i < tank.getTanks(); i++) {
        FluidStack fluid = tank.getFluidInTank(i);
        if (!fluid.isEmpty()) {
          addFluid(lines, label, fluid, tank.getTankCapacity(i));
        }
      }
    }

    private static void addFluid(List<String> lines, String label, FluidStack fluid, int capacity) {
      String amount = fluid.getAmount() + (capacity > 0 ? "/" + capacity : "") + " mB";
      lines.add(label + ": " + fluid.getHoverName().getString() + " " + amount);
    }

    private static void addFuel(List<String> lines, FuelModule fuelModule) {
      if (fuelModule.hasFuel()) {
        lines.add("Burn: " + fuelModule.getFuel() + " ticks, " + fuelModule.getTemperature() + " C");
      }
      List<FuelInfo> fuelInfos = fuelModule.getFuelInfos();
      if (!fuelInfos.isEmpty()) {
        int totalFuel = 0;
        int totalCapacity = 0;
        for (FuelInfo fuelInfo : fuelInfos) {
          totalFuel += fuelInfo.getTotalAmount();
          totalCapacity = Math.max(totalCapacity, fuelInfo.getCapacity());
        }
        lines.add("Fuel tanks: " + totalFuel + "/" + totalCapacity + " mB");
        for (FuelInfo fuelInfo : fuelInfos) {
          lines.add(" - " + fuelInfo.getFluid().getHoverName().getString() + " "
            + fuelInfo.getTotalAmount() + " mB, " + fuelInfo.getTemperature() + " C");
        }
      }
    }

    private static void addItem(List<String> lines, String label, ItemStack stack) {
      if (!stack.isEmpty()) {
        lines.add(label + ": " + stack.getHoverName().getString() + " x" + stack.getCount());
      }
    }
  }
}
