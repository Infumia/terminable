package tr.com.infumia.terminable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine composite terminable.
 */
public interface CompositeTerminable extends Terminable, TerminableConsumer, Reset {
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
  void closeSpecific(@NotNull AutoCloseable closeable) throws CompositeClosingException;

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
  default CompositeTerminable withAll(@NotNull final AutoCloseable... closeables) {
    for (final AutoCloseable closeable : closeables) {
      this.with(closeable);
    }
    return this;
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
  default CompositeTerminable withAll(@NotNull final Iterable<? extends AutoCloseable> closeables) {
    for (final AutoCloseable closeable : closeables) {
      this.with(closeable);
    }
    return this;
  }

  /**
   * a simple implementation for {@link CompositeTerminable}.
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  final class Simple implements CompositeTerminable {

    /**
     * the closeables.
     */
    private final Deque<AutoCloseable> closeables = new ConcurrentLinkedDeque<>();

    @Override
    public void close() throws CompositeClosingException {
      final List<Exception> caught = new ArrayList<Exception>();
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
      final List<Exception> caught = new ArrayList<Exception>();
      this.closeables.removeIf(c -> {
          final boolean check = c.equals(closeable);
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
          if (!(closeable instanceof Terminable)) {
            return false;
          }
          if (closeable instanceof Reset) {
            ((Reset) closeable).reset();
          }
          return ((Terminable) closeable).closed();
        });
    }
  }
}
