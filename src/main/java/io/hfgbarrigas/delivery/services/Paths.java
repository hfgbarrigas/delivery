package io.hfgbarrigas.delivery.services;

import io.hfgbarrigas.delivery.domain.db.Path;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface Paths {
    List<Path> getPaths(@NotNull String start, @NotNull String end);
    List<Path> getPaths(@NotNull String start, @NotNull String end, @Nullable Integer time);
    List<Path> getPaths(@NotNull String start, @NotNull String end, @Nullable Integer time, @Nullable Integer cost);
    List<Path> getPaths(@NotNull String start, @NotNull String end, @Nullable Integer time, @Nullable Integer cost, @Nullable String algorithm);
}
