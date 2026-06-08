package slimeknights.tconstruct.common.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.util.concurrent.CompletableFuture;

/** Wraps vanilla data providers whose final names collide across TConstruct modules. */
public record NamedDataProvider(String getName, DataProvider wrapped) implements DataProvider {
  @Override
  public CompletableFuture<?> run(CachedOutput output) {
    return wrapped.run(output);
  }
}
