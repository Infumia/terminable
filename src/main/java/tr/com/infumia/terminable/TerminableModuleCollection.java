package tr.com.infumia.terminable;

import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine terminable module collection.
 */
public interface TerminableModuleCollection extends TerminableModule {
  /**
   * gets the modules.
   *
   * @return modules.
   */
  @NotNull
  List<TerminableModule> modules();

  @Override
  default void setup(@NotNull final TerminableConsumer consumer) {
    for (final TerminableModule module : this.modules()) {
      module.bindModuleWith(consumer);
    }
  }

  /**
   * an abstract implementation of {@link TerminableModuleCollection}.
   */
  @Getter
  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  abstract class Base implements TerminableModuleCollection {

    @NotNull
    private final List<TerminableModule> modules;

    /**
     * ctor.
     *
     * @param modules The modules to bind.
     */
    protected Base(@NotNull final TerminableModule... modules) {
      this(Arrays.asList(modules));
    }
  }
}
