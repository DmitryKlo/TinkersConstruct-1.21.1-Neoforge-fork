package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** Registers item capabilities for tool stacks */
public class ToolCapabilityProvider {
  private static final List<BiFunction<ItemStack,Supplier<? extends IToolStackView>,IToolCapabilityProvider>> PROVIDER_CONSTRUCTORS = new ArrayList<>();

  private ToolCapabilityProvider() {}

  /** Registers tool item capabilities */
  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    for (Item item : BuiltInRegistries.ITEM) {
      if (item instanceof IModifiable) {
        event.registerItem(Capabilities.FluidHandler.ITEM, ToolCapabilityProvider::getFluidHandler, item);
        event.registerItem(Capabilities.ItemHandler.ITEM, ToolCapabilityProvider::getItemHandler, item);
        event.registerItem(Capabilities.EnergyStorage.ITEM, ToolCapabilityProvider::getEnergyStorage, item);
        event.registerItem(BlockItemProviderCapability.CAPABILITY, ToolCapabilityProvider::getBlockItemProvider, item);
      }
    }
  }

  /** Creates a lazy tool stack supplier that refreshes NBT on access */
  private static Supplier<ToolStack> createToolSupplier(ItemStack stack) {
    Lazy<ToolStack> tool = Lazy.of(() -> {
      ToolStack toolStack = ToolStack.from(stack);
      toolStack.refreshTag(stack);
      return toolStack;
    });
    return tool::get;
  }

  @Nullable
  private static IFluidHandlerItem getFluidHandler(ItemStack stack, Void ctx) {
    Supplier<ToolStack> tool = createToolSupplier(stack);
    for (IToolCapabilityProvider provider : getProviders(stack, tool)) {
      provider.clearCache();
      IFluidHandlerItem handler = provider.getFluidHandler(tool.get());
      if (handler != null) {
        return handler;
      }
    }
    return null;
  }

  @Nullable
  private static IItemHandler getItemHandler(ItemStack stack, Void ctx) {
    Supplier<ToolStack> tool = createToolSupplier(stack);
    for (IToolCapabilityProvider provider : getProviders(stack, tool)) {
      provider.clearCache();
      IItemHandler handler = provider.getItemHandler(tool.get());
      if (handler != null) {
        return handler;
      }
    }
    return null;
  }

  @Nullable
  private static IEnergyStorage getEnergyStorage(ItemStack stack, Void ctx) {
    Supplier<ToolStack> tool = createToolSupplier(stack);
    for (IToolCapabilityProvider provider : getProviders(stack, tool)) {
      IEnergyStorage storage = provider.getEnergyStorage(tool.get());
      if (storage != null) {
        return storage;
      }
    }
    return null;
  }

  @Nullable
  private static BlockItemProviderCapability getBlockItemProvider(ItemStack stack, Void ctx) {
    Supplier<ToolStack> tool = createToolSupplier(stack);
    for (IToolCapabilityProvider provider : getProviders(stack, tool)) {
      BlockItemProviderCapability blockProvider = provider.getBlockItemProvider(tool.get());
      if (blockProvider != null) {
        return blockProvider;
      }
    }
    return null;
  }

  private static List<IToolCapabilityProvider> getProviders(ItemStack stack, Supplier<? extends IToolStackView> tool) {
    return PROVIDER_CONSTRUCTORS.stream()
      .map(con -> con.apply(stack, tool))
      .filter(Objects::nonNull)
      .toList();
  }

  /** Registers a tool capability provider constructor. Every new tool will call this constructor to create your provider.
   * Is it valid for this constructor to return null, just note that it will not be called a second time if the tools state changes. Thus you should avoid conditioning on anything other than item type */
  public static void register(BiFunction<ItemStack,Supplier<? extends IToolStackView>,IToolCapabilityProvider> constructor) {
    PROVIDER_CONSTRUCTORS.add(constructor);
  }

  /** Interface to get a capability on a tool */
  public interface IToolCapabilityProvider {
    /** Gets a fluid handler on the given tool */
    @Nullable
    default IFluidHandlerItem getFluidHandler(IToolStackView tool) {
      return null;
    }

    /** Gets an item handler on the given tool */
    @Nullable
    default IItemHandler getItemHandler(IToolStackView tool) {
      return null;
    }

    /** Gets an energy storage on the given tool */
    @Nullable
    default IEnergyStorage getEnergyStorage(IToolStackView tool) {
      return null;
    }

    /** Gets a block item provider on the given tool */
    @Nullable
    default BlockItemProviderCapability getBlockItemProvider(IToolStackView tool) {
      return null;
    }

    /** Called to clear the cache of the provider */
    default void clearCache() {}
  }
}
