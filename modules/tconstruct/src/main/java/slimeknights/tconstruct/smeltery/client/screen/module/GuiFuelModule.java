package slimeknights.tconstruct.smeltery.client.screen.module;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import slimeknights.mantle.client.screen.ScalableElementScreen;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule.FuelInfo;
import slimeknights.tconstruct.smeltery.client.screen.IScreenWithFluidTank;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * GUI component handling the fuel module
 */
public class GuiFuelModule implements IScreenWithFluidTank, ClickableTankModule {
  // tooltips
  private static final String TOOLTIP_TEMPERATURE = TConstruct.makeTranslationKey("gui", "melting.fuel.temperature");
  private static final List<Component> TOOLTIP_NO_TANK = Collections.singletonList(Component.translatable(TConstruct.makeTranslationKey("gui", "melting.fuel.no_tank")));
  private static final List<Component> TOOLTIP_NO_FUEL = Collections.singletonList(Component.translatable(TConstruct.makeTranslationKey("gui", "melting.fuel.empty")));
  private static final Component TOOLTIP_INVALID_FUEL = Component.translatable(TConstruct.makeTranslationKey("gui", "melting.fuel.invalid")).withStyle(ChatFormatting.RED);
  private static final Component TOOLTIP_SOLID_FUEL = Component.translatable(TConstruct.makeTranslationKey("gui", "melting.fuel.solid"));

  private final AbstractContainerScreen<?> screen;
  private final FuelModule fuelModule;
  /** location to draw the tank */
  private final int x, y, width, height;
  /** Location of the fluid for JEI */
  private final Rect2i fluidLoc;
  /** location to draw the fire */
  private final int fireX, fireY;
  /** If true, UI has a fuel slot */
  private final boolean hasFuelSlot;
  /** Scalable fire instance */
  private final ScalableElementScreen fire;

  private FuelInfo fuelInfo = FuelInfo.EMPTY;
  private List<FuelInfo> fuelInfos = List.of();

  public GuiFuelModule(AbstractContainerScreen<?> screen, FuelModule fuelModule, int x, int y, int width, int height, int fireX, int fireY, boolean hasFuelSlot, ResourceLocation background) {
    this.screen = screen;
    this.fuelModule = fuelModule;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.fluidLoc = new Rect2i(x - 1, y - 1, width + 2, height + 2);
    this.fireX = fireX;
    this.fireY = fireY;
    this.hasFuelSlot = hasFuelSlot;
    this.fire = makeFire(background);
  }

  @Override
  public AbstractContainerMenu getMenu() {
    return screen.getMenu();
  }

  @Override
  public boolean isHovered(int checkX, int checkY) {
    return GuiUtil.isHovered(checkX, checkY, x - 1, y - 1, width + 2, height + 2);
  }

  /** Gets the current height of the fluid */
  private int getFluidHeight() {
    int fluidHeight = 0;
    for (int segmentHeight : calcFuelHeights()) {
      fluidHeight += segmentHeight;
    }
    return Math.min(height, fluidHeight);
  }

  @Override
  public boolean isFluidHovered(int checkY) {
    return checkY > (y + height) - getFluidHeight();
  }


  /**
   * Draws the fuel at the correct location
   * @param graphics  Matrix stack instance
   */
  public void draw(GuiGraphics graphics) {
    // draw fire
    int fuel = fuelModule.getFuel();
    int fuelQuality = fuelModule.getFuelQuality();
    if (fuel > 0 && fuelQuality > 0) {
      fire.drawScaledYUp(graphics, fireX + screen.getGuiLeft(), fireY + screen.getGuiTop(), 14 * fuel / fuelQuality);
    }

    // draw tank second, it changes the image
    // store fuel info into a field for other methods, this one updates most often
    if (!hasFuelSlot) {
      refreshFuelInfo();
      if (!fuelInfos.isEmpty()) {
        int[] heights = calcFuelHeights();
        int segmentY = y + height;
        for (int i = 0; i < fuelInfos.size(); i++) {
          int fluidHeight = Math.min(heights[i], segmentY - y);
          if (fluidHeight > 0) {
            segmentY -= fluidHeight;
            GuiUtil.renderTiledFluid(graphics.pose(), screen, fuelInfos.get(i).getFluid(), x, segmentY, width, fluidHeight, 100);
          }
        }
      }
    }
  }

  /**
   * Highlights the hovered fuel
   * @param graphics  GuiGraphics instance
   * @param checkX    Top corner relative mouse X
   * @param checkY    Top corner relative mouse Y
   */
  public void renderHighlight(GuiGraphics graphics, int checkX, int checkY) {
    if (isHovered(checkX, checkY)) {
      // if there is a fuel slot, render highlight lower
      if (hasFuelSlot) {
        if (checkY > y + 18) {
          GuiUtil.renderHighlight(graphics, x, y + 18, width, height - 18);
        }
      } else {
        // full fluid highlight
        GuiUtil.renderHighlight(graphics, x, y, width, height);
      }
    }
  }

  /**
   * Adds the tooltip for the fuel
   * @param graphics  GuiGraphics instance
   * @param mouseX    Mouse X position
   * @param mouseY    Mouse Y position
   */
  public void addTooltip(GuiGraphics graphics, int mouseX, int mouseY, boolean hasTank) {
    int checkX = mouseX - screen.getGuiLeft();
    int checkY = mouseY - screen.getGuiTop();

    if (isHovered(checkX, checkY)) {
      hasTank |= hasKnownTank();
      List<Component> tooltip;
      // if an item or we have a fuel slot, do item tooltip
      if (hasFuelSlot || fuelInfo.isItem()) {
        // if there is a fuel slot, start below the fuel slot
        if (!hasFuelSlot || checkY > y + 18) {
          if (hasTank) {
            // no invalid fuel, we assume the slot is validated (hasFuelSlot is only true for the heater which validates)
            int temperature = fuelModule.getTemperature();
            if (temperature > 0) {
              tooltip = Arrays.asList(TOOLTIP_SOLID_FUEL, Component.translatable(TOOLTIP_TEMPERATURE, temperature).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            } else {
              tooltip = TOOLTIP_NO_FUEL;
            }
          } else {
            tooltip = TOOLTIP_NO_TANK;
          }
        } else {
          tooltip = Collections.emptyList();
        }
      } else if (!fuelInfos.isEmpty()) {
        FuelInfo hoveredFuel = getFuelInfoAt(checkY);
        if (hoveredFuel == null) {
          hoveredFuel = fuelInfo;
        }
        FluidStack fluid = hoveredFuel.getFluid();
        tooltip = FluidTooltipHandler.getFluidTooltip(fluid, hoveredFuel.getTotalAmount());
        int temperature = hoveredFuel.getTemperature();
        if (temperature > 0) {
          tooltip.add(1, Component.translatable(TOOLTIP_TEMPERATURE, temperature).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
          tooltip.add(1, TOOLTIP_INVALID_FUEL);
        }
      } else {
        tooltip = hasTank ? TOOLTIP_NO_FUEL : TOOLTIP_NO_TANK;
      }

      graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
    }
  }

  @Override
  @Nullable
  public FluidLocation getFluidUnderMouse(int checkX, int checkY) {
    if (!hasFuelSlot && isHovered(checkX, checkY) && !fuelInfos.isEmpty()) {
      FuelInfo hoveredFuel = getFuelInfoAt(checkY);
      if (hoveredFuel != null) {
        return new FluidLocation(hoveredFuel.getFluid(), fluidLoc);
      }
    }
    return null;
  }

  /** Gets the fuel segment under the mouse */
  @Nullable
  private FuelInfo getFuelInfoAt(int checkY) {
    if (fuelInfos.isEmpty()) {
      return null;
    }
    int[] heights = calcFuelHeights();
    int segmentY = y + height;
    for (int i = 0; i < fuelInfos.size(); i++) {
      int fluidHeight = heights[i];
      segmentY -= fluidHeight;
      if (fluidHeight > 0 && checkY > segmentY) {
        return fuelInfos.get(i);
      }
    }
    return null;
  }

  /** Refreshes fuel info without hiding the last known tank during a transient client-side structure reload. */
  private void refreshFuelInfo() {
    List<FuelInfo> current = fuelModule.getFuelInfos();
    if (!current.isEmpty()) {
      fuelInfos = current;
      fuelInfo = current.get(0);
    } else if (!hasBackingTank() && !fuelModule.hasFuel()) {
      fuelInfos = List.of();
      fuelInfo = FuelInfo.EMPTY;
    }
  }

  /** Checks if the backing module still knows about at least one fuel tank. */
  private boolean hasKnownTank() {
    return fuelModule.hasFuel() || !fuelInfos.isEmpty() || hasBackingTank();
  }

  /** Checks if the backing fluid handler reports any tank slots. */
  private boolean hasBackingTank() {
    return fuelModule instanceof IFluidHandler fluidHandler && fluidHandler.getTanks() > 0;
  }

  /** Gets pixel heights for each visible fuel segment. */
  private int[] calcFuelHeights() {
    int[] segmentHeights = new int[fuelInfos.size()];
    if (fuelInfos.isEmpty() || height <= 0) {
      return segmentHeights;
    }

    int capacity = fuelInfos.get(0).getCapacity();
    if (capacity <= 0) {
      return segmentHeights;
    }

    List<FluidStack> fluids = fuelInfos.stream()
        .map(info -> info.getFluid().copyWithAmount(info.getTotalAmount()))
        .toList();
    int[] heights = GuiSmelteryTank.calcLiquidHeights(fluids, capacity, height, 1);
    if (heights.length == segmentHeights.length) {
      return heights;
    }
    return segmentHeights;
  }

  /** Creates the fire element from the standard location */
  public static ScalableElementScreen makeFire(ResourceLocation background) {
    return new ScalableElementScreen(background, 176, 136, 14, 14, 256, 256);
  }
}
