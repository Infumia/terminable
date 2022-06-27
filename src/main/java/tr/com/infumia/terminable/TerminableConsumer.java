package tr.com.infumia.terminable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine terminable consumers.
 */
@FunctionalInterface
@SuppressWarnings("UnusedReturnValue")
public interface TerminableConsumer {

  /**
   * binds the terminable.
   *
   * @param terminable the terminable to bind.
   * @param <T> type of the terminable.
   *
   * @return terminable.
   */
  @NotNull
  @Contract("_ -> param1")
  <T extends AutoCloseable> T bind(
    @NotNull T terminable
  );

  /**
   * binds the module.
   *
   * @param module the module to bind.
   * @param <T> type of the module.
   *
   * @return module.
   */
  @NotNull
  @Contract("_ -> param1")
  default <T extends TerminableModule> T bindModule(
    @NotNull final T module
  ) {
    module.setup(this);
    return module;
  }
}
