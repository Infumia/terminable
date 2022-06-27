package tr.com.infumia.terminable;

import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine terminable modules.
 */
public interface TerminableModule {
  /**
   * binds the terminable consumer.
   *
   * @param consumer the consumer to bind.
   */
  default void bindModuleWith(@NotNull final TerminableConsumer consumer) {
    consumer.bindModule(this);
  }

  /**
   * setups the consumer.
   *
   * @param consumer the consumer to set up.
   */
  void setup(@NotNull TerminableConsumer consumer);
}
