package slimeknights.mantle.util.compat;

@FunctionalInterface
public interface NonNullFunction<T, R> {
  R apply(T value);
}
