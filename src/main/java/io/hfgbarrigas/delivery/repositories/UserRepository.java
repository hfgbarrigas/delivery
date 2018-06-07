package io.hfgbarrigas.delivery.repositories;

import io.hfgbarrigas.delivery.domain.db.User;
import io.hfgbarrigas.delivery.domain.db.projections.SimpleUser;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RepositoryRestResource(path = "users", collectionResourceRel = "users", excerptProjection = SimpleUser.class)
public interface UserRepository extends Neo4jRepository<User, Long> {

    Optional<User> findUserByUsername(@Param("username") String username);

    @Override
    @RestResource(exported = false)
    <S extends User> S save(S s, int depth);

    @Override
    @RestResource(exported = false)
    <S extends User> Iterable<S> save(Iterable<S> entities, int depth);

    @Override
    @RestResource(exported = false)
    <S extends User> S save(S s);

    @Override
    @RestResource(exported = false)
    void delete(User user);

    @Override
    @RestResource(exported = false)
    void deleteAll();

    @Override
    @RestResource(exported = false)
    <S extends User> Iterable<S> saveAll(Iterable<S> iterable);

    @Override
    @RestResource(exported = false)
    void deleteById(Long aLong);

    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends User> iterable);
}
