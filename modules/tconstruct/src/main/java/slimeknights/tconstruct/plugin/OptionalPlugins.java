package slimeknights.tconstruct.plugin;

import net.neoforged.bus.api.IEventBus;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.TConstruct;

/** Loads optional mod integrations via reflection so they can be excluded from compile when deps are unavailable. */
public final class OptionalPlugins {
  private OptionalPlugins() {}

  public static void loadImmersiveEngineering(IEventBus bus) {
    registerOnBus(bus, "slimeknights.tconstruct.plugin.ImmersiveEngineeringPlugin");
  }

  public static void loadJsonThings() {
    invokeStatic("slimeknights.tconstruct.plugin.jsonthings.JsonThingsPlugin", "onConstruct");
  }

  public static void loadDiet() {
    invokeStatic("slimeknights.tconstruct.plugin.DietPlugin", "onConstruct");
  }

  public static void loadCraftingTweaks() {
    invokeStatic("slimeknights.tconstruct.plugin.craftingtweaks.CraftingTweaksPlugin", "onConstruct");
  }

  public static void loadDummmmmmy(IEventBus bus) {
    registerOnBus(bus, "slimeknights.tconstruct.plugin.DummmmmmyPlugin");
  }

  private static void registerOnBus(IEventBus bus, String className) {
    try {
      Object instance = Class.forName(className).getConstructor().newInstance();
      bus.register(instance);
    } catch (ClassNotFoundException ignored) {
      // optional plugin excluded from build
    } catch (ReflectiveOperationException e) {
      logError(className, e);
    }
  }

  private static void invokeStatic(String className, String method) {
    try {
      Class.forName(className).getMethod(method).invoke(null);
    } catch (ClassNotFoundException ignored) {
      // optional plugin excluded from build
    } catch (ReflectiveOperationException e) {
      logError(className, e);
    }
  }

  private static void logError(String className, ReflectiveOperationException e) {
    Logger logger = TConstruct.LOG;
    logger.error("Failed to load optional plugin {}", className, e);
  }
}
