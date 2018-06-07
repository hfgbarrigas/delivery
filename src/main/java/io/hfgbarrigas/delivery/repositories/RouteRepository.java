package io.hfgbarrigas.delivery.repositories;

import io.hfgbarrigas.delivery.domain.db.Route;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "routes", collectionResourceRel = "routes")
public interface RouteRepository extends Neo4jRepository<Route, Long> {

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    <S extends Route> S save(S s, int depth);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    <S extends Route> Iterable<S> save(Iterable<S> entities, int depth);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    <S extends Route> S save(S s);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    void delete(Route route);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    <S extends Route> Iterable<S> saveAll(Iterable<S> iterable);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    void deleteById(Long aLong);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    void deleteAll(Iterable<? extends Route> iterable);
}
