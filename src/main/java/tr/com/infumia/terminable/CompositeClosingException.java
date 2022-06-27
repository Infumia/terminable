package tr.com.infumia.terminable;

import java.util.Collection;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * an exception class thrown to propagate exceptions thrown by composite terminable.
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class CompositeClosingException extends Exception {

  /**
   * the causes.
   */
  @NotNull
  Iterable<? extends Throwable> causes;

  /**
   * ctor.
   *
   * @param causes the causes.
   */
  public CompositeClosingException(
    @NotNull final Collection<? extends Throwable> causes
  ) {
    super("Exception(s) occurred whilst closing: " + causes);
    if (causes.isEmpty()) {
      throw new IllegalStateException("No causes");
    }
    this.causes = Collections.unmodifiableCollection(causes);
  }

  /**
   * prints all stack traces.
   */
  public void printAllStackTraces() {
    this.printStackTrace();
    for (final var cause : this.causes) {
      cause.printStackTrace();
    }
  }
}
