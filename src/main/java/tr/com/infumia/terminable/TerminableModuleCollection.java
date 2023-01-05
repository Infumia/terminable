package tr.com.infumia.terminable;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

public interface TerminableModuleCollection extends TerminableModule {
  @NotNull
  List<TerminableModule> modules();

  @Override
  default void setup(@NotNull final TerminableConsumer consumer) {
    for (final var module : this.modules()) {
      module.bindModuleWith(consumer);
    }
  }

  @Getter
  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  abstract class Base implements TerminableModuleCollection {

    @NotNull
    List<TerminableModule> modules;
  }
}
