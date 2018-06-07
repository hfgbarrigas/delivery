package io.hfgbarrigas.delivery.services;

import io.hfgbarrigas.delivery.domain.db.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface Users extends UserDetailsService {
    Optional<User> saveUser(io.hfgbarrigas.delivery.domain.api.User user);
    Optional<User> updateUser(Long id, io.hfgbarrigas.delivery.domain.api.User user);
    void deleteUser(Long id);
    Optional<User> findUserByUsername(String username);
    Optional<User> findById(Long id);
}
