package tr.com.infumia.terminable;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine terminable.
 */
@FunctionalInterface
public interface Terminable extends AutoCloseable {

  /**
   * binds with the consumer.
   *
   * @param consumer the consumer to bind.
   */
  default void bindWith(
    @NotNull final TerminableConsumer consumer
  ) {
    consumer.bind(this);
  }

  /**
   * closes the returns the exception if occurs.
   *
   * @return occurred exception when closing.
   */
  @NotNull
  default Optional<? extends Exception> closeSilently() {
    try {
      this.close();
      return Optional.empty();
    } catch (final Exception e) {
      return Optional.of(e);
    }
  }

  /**
   * closes and reports the exception if occurs.
   */
  default void closeUnchecked() {
    this.closeSilently().ifPresent(Throwable::printStackTrace);
  }

  /**
   * checks if it's closed or not.
   *
   * @return {@code true} if closed.
   */
  default boolean closed() {
    return false;
  }
}
