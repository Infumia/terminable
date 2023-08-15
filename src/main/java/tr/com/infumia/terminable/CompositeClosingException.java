package tr.com.infumia.terminable;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * an exception class thrown to propagate exceptions thrown by composite terminable.
 */
@Getter
public final class CompositeClosingException extends Exception {

  /**
   * the causes.
   */
  @NotNull
  private final Iterable<? extends Throwable> causes;

  /**
   * ctor.
   *
   * @param causes the causes.
   */
  CompositeClosingException(@NotNull final Collection<? extends Throwable> causes) {
    super("Exception(s) occurred whilst closing: " + causes);
    if (causes.isEmpty()) {
      throw new IllegalStateException("No causes");
    }
    this.causes = Collections.unmodifiableCollection(causes);
  }

  /**
   * prints all stack traces.
   */
  void printAllStackTraces() {
    this.printStackTrace();
    for (final Throwable cause : this.causes) {
      cause.printStackTrace();
    }
  }
}
