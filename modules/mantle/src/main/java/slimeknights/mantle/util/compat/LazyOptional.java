package slimeknights.mantle.util.compat;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class LazyOptional<T> {
  private static final LazyOptional<?> EMPTY = new LazyOptional<>(null);

  @Nullable
  private final Supplier<? extends T> supplier;

  private LazyOptional(@Nullable Supplier<? extends T> supplier) {
    this.supplier = supplier;
  }

  public static <T> LazyOptional<T> of(Supplier<? extends T> supplier) {
    return new LazyOptional<>(supplier);
  }

  @SuppressWarnings("unchecked")
  public static <T> LazyOptional<T> empty() {
    return (LazyOptional<T>) EMPTY;
  }

  public boolean isPresent() {
    return supplier != null && supplier.get() != null;
  }

  public T orElse(T fallback) {
    T value = supplier == null ? null : supplier.get();
    return value == null ? fallback : value;
  }

  public Optional<T> resolve() {
    return supplier == null ? Optional.empty() : Optional.ofNullable(supplier.get());
  }

  public <R> Optional<R> map(Function<? super T, ? extends R> mapper) {
    return resolve().map(mapper);
  }

  @SuppressWarnings("unchecked")
  public <R> LazyOptional<R> cast() {
    return (LazyOptional<R>) this;
  }
}
