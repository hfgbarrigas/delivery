package io.hfgbarrigas.delivery.repositories;

import io.hfgbarrigas.delivery.domain.db.Place;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

@RepositoryRestResource(path = "places", collectionResourceRel = "places")
public interface PlaceRepository extends Neo4jRepository<Place, Long> {

    Optional<Place> findPlaceByName(@Param("name") String name);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    <S extends Place> S save(S s, @Param("depth") int depth);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    <S extends Place> Iterable<S> save(Iterable<S> entities, @Param("depth") int depth);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    <S extends Place> S save(S s);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    void delete(Place place);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    <S extends Place> Iterable<S> saveAll(Iterable<S> iterable);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    void deleteById(Long aLong);

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    void deleteAll(Iterable<? extends Place> iterable);
}
