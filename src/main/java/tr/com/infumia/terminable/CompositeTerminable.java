package tr.com.infumia.terminable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine composite terminable.
 */
public interface CompositeTerminable
  extends Terminable, TerminableConsumer, Reset {
  /**
   * creates a simple composite terminable.
   *
   * @return composite terminable.
   */
  @NotNull
  static CompositeTerminable simple() {
    return new Simple();
  }

  @NotNull
  @Override
  default <T extends AutoCloseable> T bind(@NotNull final T terminable) {
    this.with(terminable);
    return terminable;
  }

  @Override
  void close() throws CompositeClosingException;

  @NotNull
  @Override
  default Optional<CompositeClosingException> closeSilently() {
    try {
      this.close();
      return Optional.empty();
    } catch (final CompositeClosingException e) {
      return Optional.of(e);
    }
  }

  @Override
  default void closeUnchecked() {
    try {
      this.close();
    } catch (final CompositeClosingException e) {
      e.printAllStackTraces();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * closes the specific closeable object.
   *
   * @param closeable the closeable to close.
   *
   * @throws CompositeClosingException if something goes wrong when closing it.
   */
  void closeSpecific(@NotNull AutoCloseable closeable)
    throws CompositeClosingException;

  /**
   * binds the closeable.
   *
   * @param closeable the closeable to bind.
   *
   * @return {@code this} for the chain.
   */
  @NotNull
  @Contract("_ -> this")
  CompositeTerminable with(@NotNull AutoCloseable closeable);

  /**
   * binds all the closeable.
   *
   * @param closeables the closeables to bind.
   *
   * @return {@code this} for the chain.
   */
  @NotNull
  @Contract("_ -> this")
  default CompositeTerminable withAll(
    @NotNull final AutoCloseable... closeables
  ) {
    return this.withAll(List.of(closeables));
  }

  /**
   * binds all the closeable.
   *
   * @param closeables the closeables to bind.
   *
   * @return {@code this} for the chain.
   */
  @NotNull
  @Contract("_ -> this")
  default CompositeTerminable withAll(
    @NotNull final Iterable<? extends AutoCloseable> closeables
  ) {
    closeables.forEach(this::with);
    return this;
  }

  /**
   * a simple implementation for {@link CompositeTerminable}.
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  final class Simple implements CompositeTerminable {

    /**
     * the closeables.
     */
    Deque<AutoCloseable> closeables = new ConcurrentLinkedDeque<>();

    @Override
    public void close() throws CompositeClosingException {
      final var caught = new ArrayList<Exception>();
      AutoCloseable ac;
      while ((ac = this.closeables.poll()) != null) {
        try {
          ac.close();
        } catch (final Exception e) {
          caught.add(e);
        }
      }
      if (!caught.isEmpty()) {
        throw new CompositeClosingException(caught);
      }
    }

    @Override
    public void closeSpecific(@NotNull final AutoCloseable closeable)
      throws CompositeClosingException {
      final var caught = new ArrayList<Exception>();
      this.closeables.removeIf(c -> {
          final var check = c.equals(closeable);
          if (check) {
            try {
              c.close();
            } catch (final Exception e) {
              caught.add(e);
            }
          }
          return check;
        });
      if (!caught.isEmpty()) {
        throw new CompositeClosingException(caught);
      }
    }

    @NotNull
    @Override
    public CompositeTerminable with(@NotNull final AutoCloseable closeable) {
      this.closeables.addFirst(closeable);
      return this;
    }

    @Override
    public void reset() {
      this.closeables.removeIf(closeable -> {
          if (!(closeable instanceof Terminable terminable)) {
            return false;
          }
          if (closeable instanceof Reset reset) {
            reset.reset();
          }
          return terminable.closed();
        });
    }
  }
}
