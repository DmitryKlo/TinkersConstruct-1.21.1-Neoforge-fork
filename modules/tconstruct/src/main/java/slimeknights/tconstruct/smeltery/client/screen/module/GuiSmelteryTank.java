package slimeknights.tconstruct.smeltery.client.screen.module;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.smeltery.block.entity.tank.SmelteryTank;
import slimeknights.tconstruct.smeltery.client.screen.IScreenWithFluidTank;
import slimeknights.tconstruct.smeltery.network.SmelteryFluidClickedPacket;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Helper class to draw the smeltery tank in UIs
 */
public class GuiSmelteryTank implements IScreenWithFluidTank {
  // fluid tooltips
  public static final Component TOOLTIP_CAPACITY = TConstruct.makeTranslation("gui", "melting.capacity");
  public static final Component TOOLTIP_AVAILABLE = TConstruct.makeTranslation("gui", "melting.available");
  public static final Component TOOLTIP_USED = TConstruct.makeTranslation("gui", "melting.used");

  private final AbstractContainerScreen<?> parent;
  private final SmelteryTank<?> tank;
  private final int x, y, width, height;
  private final BiConsumer<Integer,List<Component>> formatter;

  private int[] liquidHeights;

  public GuiSmelteryTank(AbstractContainerScreen<?> parent, SmelteryTank<?> tank, int x, int y, int width, int height, ResourceLocation tooltipId) {
    this.parent = parent;
    this.tank = tank;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.formatter = (amount, tooltip) -> FluidTooltipHandler.appendNamedList(tooltipId, amount, tooltip);
  }

  /**
   * Calculates the heights of the liquids
   * @param   refresh  If true, refresh the heights
   * @return  Array of liquid heights at each index
   */
  private int[] calcLiquidHeights(boolean refresh) {
    assert tank != null;
    if (liquidHeights == null || refresh) {
      liquidHeights = calcLiquidHeights(tank.getFluids(), Math.max(tank.getContained(), tank.getCapacity()), height, 1);
    }
    return liquidHeights;
  }

  /**
   * Checks if a position is within the tank
   * @param checkX  X position to check
   * @param checkY  Y position to check
   * @return  True if within the tank
   */
  public boolean withinTank(int checkX, int checkY) {
    return x <= checkX && checkX < (x + width) && y <= checkY && checkY < (y + height);
  }

  /**
   * Renders the smeltery tank
   * @param matrices  Matrix stack instance
   */
  public void renderFluids(PoseStack matrices) {
    // draw liquids
    if (tank.getContained() > 0) {
      int[] heights = calcLiquidHeights(true);

      int bottom = y + height;
      for (int i = 0; i < heights.length; i++) {
        int fluidH = Math.min(heights[i], bottom - y);
        if (fluidH > 0) {
          FluidStack liquid = tank.getFluids().get(i);
          GuiUtil.renderTiledFluid(matrices, parent, liquid, x, bottom - fluidH, width, fluidH, 100);
          bottom -= fluidH;
        }
      }
    } else if (liquidHeights != null && liquidHeights.length > 0) {
      liquidHeights = new int[0];
    }
  }

  /**
   * Gets the fluid under the mouse at the given Y position relative to the tank bottom
   * @param heights  Fluids heights
   * @param y  Y position to check
   * @return  Fluid index under mouse, or -1 if no fluid
   */
  private int getFluidHovered(int[] heights, int y) {
    for (int i = 0; i < heights.length; i++) {
      if (y < heights[i]) {
        return i;
      }
      y -= heights[i];
    }

    return -1;
  }

  /**
   * Gets the fluid under the mouse at the given Y mouse position
   * @param heights  Fluids heights
   * @param checkY   Mouse Y position
   * @return  Fluid index under mouse, or -1 if no fluid
   */
  private int getFluidFromMouse(int[] heights, int checkY) {
    return getFluidHovered(heights, (y + height) - checkY - 1);
  }

  /**
   * Renders a highlight on the hovered fluid
   * @param graphics  GuiGraphics instance
   * @param mouseX    Mouse X
   * @param mouseY    Mouse Y
   */
  public void renderHighlight(GuiGraphics graphics, int mouseX, int mouseY) {
    int checkX = mouseX - parent.getGuiLeft();
    int checkY = mouseY - parent.getGuiTop();
    if (withinTank(checkX, checkY)) {
      if (tank.getContained() == 0) {
        GuiUtil.renderHighlight(graphics, x, y, width, height);
      } else {
        int[] heights = calcLiquidHeights(false);
        int top = this.y + height;
        for (int height : heights) {
          top -= height;
          if (top <= checkY) {
            GuiUtil.renderHighlight(graphics, x, top, width, height);
            return;
          }
        }
        GuiUtil.renderHighlight(graphics, x, y, width, top - y);
      }
    }
  }

  /**
   * Gets the tooltip for the tank based on the given mouse position
   * @param graphics  GuiGraphics instance
   * @param mouseX    Mouse X
   * @param mouseY    Mouse Y
   */
  public void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    // Liquids
    int checkX = mouseX - parent.getGuiLeft();
    int checkY = mouseY - parent.getGuiTop();
    if (withinTank(checkX, checkY)) {
      int hovered = tank.getContained() == 0 ? -1 : getFluidFromMouse(calcLiquidHeights(false), checkY);
      List<Component> tooltip;
      if (hovered == -1) {
        BiConsumer<Integer, List<Component>> formatter = Screen.hasShiftDown() ? FluidTooltipHandler.BUCKET_FORMATTER : this.formatter;

        tooltip = new ArrayList<>();
        tooltip.add(TOOLTIP_CAPACITY);

        formatter.accept(tank.getCapacity(), tooltip);
        int remaining = tank.getRemainingSpace();
        if (remaining > 0) {
          tooltip.add(TOOLTIP_AVAILABLE);
          formatter.accept(remaining, tooltip);
        }
        int used = tank.getContained();
        if (used > 0) {
          tooltip.add(TOOLTIP_USED);
          formatter.accept(used, tooltip);
        }
        FluidTooltipHandler.appendShift(tooltip);
      }
      else {
        tooltip = FluidTooltipHandler.getFluidTooltip(tank.getFluidInTank(hovered));
      }
      graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
    }
  }

  /**
   * Checks if the tank was clicked at the given location
   * @return Index clicked. -1 if in the tank and nothing was clicked. -2 if not in the tank
   */
  public int getFluidClicked(int mouseX, int mouseY) {
    if (withinTank(mouseX, mouseY)) {
      return getFluidFromMouse(calcLiquidHeights(false), mouseY);
    }
    return -2;
  }

  /**
   * Checks if the tank was clicked at the given location
   * @return Index clicked. -1 if in the tank and nothing was clicked. -2 if not in the tank
   * @deprecated use {@link #getFluidClicked(int, int)} with {@link net.minecraft.world.inventory.AbstractContainerMenu#clickMenuButton(Player, int)} and {@link net.minecraft.client.multiplayer.MultiPlayerGameMode#handleInventoryButtonClick(int, int)}
   */
  @Deprecated
  public int handleClick(int mouseX, int mouseY) {
    int index = getFluidClicked(mouseX, mouseY);
    if (index >= 0) {
      TinkerNetwork.getInstance().sendToServer(new SmelteryFluidClickedPacket(index));
    }
    return index;
  }

  @Nullable
  @Override
  public FluidLocation getFluidUnderMouse(int checkX, int checkY) {
    if (tank.getContained() > 0 && withinTank(checkX, checkY)) {
      // can't just use the helper as we need the location of the fluid
      int[] heights = calcLiquidHeights(false);
      int y = this.y + height;
      for (int i = 0; i < heights.length; i++) {
        int fluidHeight = heights[i];
        y -= fluidHeight;
        if (fluidHeight > 0 && y <= checkY) {
          return new FluidLocation(tank.getFluidInTank(i), new Rect2i(x, y, width, fluidHeight));
        }
      }
    }
    return null;
  }


  /* Utils */

  /**
   * Calculate the rendering heights for all the liquids
   *
   * @param liquids  The liquids
   * @param capacity Max capacity of smeltery, to calculate how much height one liquid takes up
   * @param height   Maximum height, basically represents how much height full capacity is
   * @param min      Minimum amount of height for a fluid. A fluid can never have less than this value height returned
   * @return Array with heights corresponding to input-list liquids
   */
  public static int[] calcLiquidHeights(List<FluidStack> liquids, int capacity, int height, int min) {
    int[] fluidHeights = new int[liquids.size()];

    if (liquids.isEmpty() || capacity <= 0 || height <= 0) {
      return fluidHeights;
    }

    long totalFluidAmount = 0;
    int visibleFluids = 0;
    for (FluidStack liquid : liquids) {
      int amount = Math.max(0, liquid.getAmount());
      if (amount > 0) {
        totalFluidAmount += amount;
        visibleFluids++;
      }
    }
    if (totalFluidAmount <= 0) {
      return fluidHeights;
    }

    int visibleMinimum = visibleFluids * Math.max(0, min);
    int totalHeight = (int)Math.ceil((double)height * Math.min(totalFluidAmount, (long)capacity) / capacity);
    totalHeight = Math.min(height, Math.max(Math.min(visibleMinimum, height), totalHeight));
    if (totalHeight <= 0) {
      return fluidHeights;
    }

    int preferredMin = Math.min(Math.max(0, min), totalHeight);
    int minHeight = visibleFluids * preferredMin <= totalHeight ? preferredMin : 0;
    long[] remainders = new long[liquids.size()];
    int sum = 0;
    for (int i = 0; i < liquids.size(); i++) {
      int amount = Math.max(0, liquids.get(i).getAmount());
      if (amount > 0) {
        long scaled = (long)totalHeight * amount;
        int liquidHeight = (int)(scaled / totalFluidAmount);
        remainders[i] = scaled % totalFluidAmount;
        if (minHeight > 0) {
          liquidHeight = Math.max(minHeight, liquidHeight);
        }
        fluidHeights[i] = Math.min(totalHeight, liquidHeight);
        sum += fluidHeights[i];
      }
    }

    while (sum < totalHeight) {
      int largestRemainder = -1;
      for (int i = 0; i < remainders.length; i++) {
        if (liquids.get(i).getAmount() > 0 && (largestRemainder == -1 || remainders[i] > remainders[largestRemainder])) {
          largestRemainder = i;
        }
      }
      if (largestRemainder == -1) {
        break;
      }
      fluidHeights[largestRemainder]++;
      remainders[largestRemainder] = -1;
      sum++;
    }

    shrinkHeightsToFit(fluidHeights, sum, totalHeight);
    return fluidHeights;
  }

  /** Shrinks the tallest liquid segments until all liquids fit inside the target height. */
  private static void shrinkHeightsToFit(int[] heights, int sum, int maxHeight) {
    while (sum > maxHeight) {
      int tallest = -1;
      for (int i = 0; i < heights.length; i++) {
        if (tallest == -1 || heights[i] > heights[tallest]) {
          tallest = i;
        }
      }
      if (tallest == -1 || heights[tallest] == 0) {
        return;
      }
      heights[tallest]--;
      sum--;
    }
  }
}
