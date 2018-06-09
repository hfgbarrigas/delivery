package io.hfgbarrigas.delivery.repositories;

import io.hfgbarrigas.delivery.domain.db.Authority;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collection;
import java.util.List;

@RepositoryRestResource(path = "authorities")
@PreAuthorize("hasAuthority('ADMIN')")
public interface AuthorityRepository extends Neo4jRepository<Authority, Long> {

    List<Authority> findAllByNameIn(Collection<String> authorities);
    Authority findByNameEquals(String authority);

}
