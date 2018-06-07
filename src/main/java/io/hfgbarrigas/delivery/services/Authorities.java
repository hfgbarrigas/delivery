package io.hfgbarrigas.delivery.services;

import io.hfgbarrigas.delivery.domain.db.Authority;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Authorities {
    Collection<Authority> addUserAuthorities(Long id, List<String> authorities);
    Optional<Authority> addUserAuthority(Long id, String authority);
    Collection<Authority> createAuthorities(List<String> authorities);
    Optional<Authority> createAuthority(String authority);
}
