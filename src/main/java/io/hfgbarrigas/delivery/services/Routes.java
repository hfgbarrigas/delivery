package io.hfgbarrigas.delivery.services;

import io.hfgbarrigas.delivery.domain.api.Route;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;

public interface Routes {
    Optional<io.hfgbarrigas.delivery.domain.db.Route> saveRoute(@NotNull Route route);
    Collection<io.hfgbarrigas.delivery.domain.db.Route> saveRoutes(@NotNull Collection<Route> routes);
}
