package io.hfgbarrigas.delivery.repositories;

import io.hfgbarrigas.delivery.domain.db.Authority;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collection;
import java.util.List;

@RepositoryRestResource(path = "authorities")
@PreAuthorize("hasAuthority('ADMIN')")
public interface AuthorityRepository extends Neo4jRepository<Authority, Long> {

    List<Authority> findAllByNameIn(Collection<String> authorities);
    Authority findByNameEquals(String authority);

    @Override
    @RestResource(exported = false)
    <S extends Authority> S save(S s, int depth);

    @Override
    @RestResource(exported = false)
    <S extends Authority> Iterable<S> save(Iterable<S> entities, int depth);

    @Override
    @RestResource(exported = false)
    <S extends Authority> S save(S s);

    @Override
    @RestResource(exported = false)
    <S extends Authority> Iterable<S> saveAll(Iterable<S> iterable);

    @Override
    @RestResource(exported = false)
    void deleteById(Long aLong);

    @Override
    @RestResource(exported = false)
    void delete(Authority authority);

    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends Authority> iterable);

    @Override
    @RestResource(exported = false)
    void deleteAll();
}
