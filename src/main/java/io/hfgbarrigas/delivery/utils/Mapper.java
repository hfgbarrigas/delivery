package io.hfgbarrigas.delivery.utils;

import io.hfgbarrigas.delivery.domain.api.Authority;
import io.hfgbarrigas.delivery.domain.api.Path;
import io.hfgbarrigas.delivery.domain.api.Place;
import io.hfgbarrigas.delivery.domain.api.Route;
import io.hfgbarrigas.delivery.domain.db.User;
import io.hfgbarrigas.delivery.exceptions.UnexpectedException;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Mapper {
    public static Path toApiPath(@NotNull io.hfgbarrigas.delivery.domain.db.Path path) {
        if (path == null) {
            return Path.builder().build();
        }
        return Path.builder()
                .cost(path.getCost())
                .time(path.getTime())
                .path(path.getPath() == null || path.getPath().isEmpty() ? Collections.emptyList() : path.getPath()
                        .stream()
                        .map(Mapper::toApiRoute)
                        .collect(toList()))
                .build();
    }

    public static User toDbUser(@NotNull io.hfgbarrigas.delivery.domain.api.User user) {
        if (user == null) {
            throw new UnexpectedException("Api user cannot be null before persisting.");
        }
        return User.builder()
                .id(user.getId())
                .createdDate(Instant.now().toEpochMilli())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .password(user.getPassword())
                .username(user.getUsername())
                .build();
    }

    private static Route toApiRoute(@NotNull io.hfgbarrigas.delivery.domain.db.Route r) {
        if (r == null) {
            return Route.builder().build();
        }
        return Route.builder()
                .id(r.getId())
                .cost(r.getCost())
                .time(r.getTime())
                .start(toApiSimplePlace(r.getStart())) //avoid circular traversals
                .destination(toApiSimplePlace(r.getDestination())) //avoid circular traversals
                .build();
    }

    private static Place toApiPlace(@NotNull io.hfgbarrigas.delivery.domain.db.Place p) {
        if (p == null) {
            return Place.builder().build();
        }
        return Place.builder()
                .routes(p.getRoutes() == null || p.getRoutes().isEmpty() ? Collections.emptyList() : p.getRoutes()
                        .stream()
                        .map(Mapper::toApiRoute)
                        .collect(Collectors.toList()))
                .name(p.getName())
                .build();
    }

    private static Place toApiSimplePlace(@NotNull io.hfgbarrigas.delivery.domain.db.Place p) {
        if (p == null) {
            return Place.builder().build();
        }
        return Place.builder()
                .name(p.getName())
                .build();
    }

    private static Authority toApiAuthority(io.hfgbarrigas.delivery.domain.db.Authority authority) {
        return Authority.builder()
                .name(authority.getName())
                .id(authority.getId())
                .build();
    }

    public static io.hfgbarrigas.delivery.domain.api.User toApiUser(User u) {
        return io.hfgbarrigas.delivery.domain.api.User
                .builder()
                .id(u.getId())
                .password(u.getPassword())
                .username(u.getUsername())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .authorities(u.getAuthorities() != null ? u.getAuthorities()
                        .stream()
                        .map(Mapper::toApiAuthority)
                        .collect(Collectors.toSet()) : Collections.emptySet())
                .build();
    }
}
