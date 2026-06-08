package slimeknights.mantle.util.compat;

@FunctionalInterface
public interface NonNullConsumer<T> {
  void accept(T value);
}
