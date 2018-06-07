package io.hfgbarrigas.delivery.services.defaults;

import io.hfgbarrigas.delivery.domain.db.Authority;
import io.hfgbarrigas.delivery.domain.db.User;
import io.hfgbarrigas.delivery.exceptions.DuplicateUserException;
import io.hfgbarrigas.delivery.exceptions.InvalidDataException;
import io.hfgbarrigas.delivery.exceptions.UnknownUserException;
import io.hfgbarrigas.delivery.repositories.UserRepository;
import io.hfgbarrigas.delivery.services.Users;
import io.hfgbarrigas.delivery.utils.Mapper;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class DefaultUsersService implements Users {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public DefaultUsersService(UserRepository userRepository,
                               BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(@NotNull String s) throws UsernameNotFoundException {
        Optional<io.hfgbarrigas.delivery.domain.db.User> user = userRepository.findUserByUsername(s);

        return user.map(u -> new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),
                u.getAuthorities() != null ? AuthorityUtils.createAuthorityList(u.getAuthorities()
                        .stream()
                        .map(Authority::getName)
                        .collect(toList())
                        .toArray(new String[u.getAuthorities().size()])) : Collections.emptyList()
        )).orElse(null);
    }

    @Override
    @Transactional
    public Optional<User> saveUser(@NotNull io.hfgbarrigas.delivery.domain.api.User user) {

        if (this.userRepository.findUserByUsername(user.getUsername()).isPresent()) {
            throw new DuplicateUserException("User already exists.");
        }

        return Optional.ofNullable(userRepository.save(Mapper.toDbUser(io.hfgbarrigas.delivery.domain.api.User.builder()
                .username(user.getUsername())
                .password(bCryptPasswordEncoder.encode(user.getPassword()))
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build())));
    }

    @Override
    @Transactional
    public Optional<io.hfgbarrigas.delivery.domain.db.User> updateUser(@NotNull Long id, @NotNull io.hfgbarrigas.delivery.domain.api.User newData) {

        //add all details to session next time, avoiding another db call
        String username = ((org.springframework.security.core.userdetails.User)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

        Optional<User> toUpdate = this.userRepository.findById(id);
        Optional<User> session = this.userRepository.findUserByUsername(username);
        User victim;

        if (!toUpdate.isPresent()) {
            throw new UnknownUserException();
        } else if (!toUpdate.get().getId().equals(session.get().getId())) {
            throw new InvalidDataException("Only the use can update its account");
        }

        victim = toUpdate.get();

        io.hfgbarrigas.delivery.domain.api.User updatedVersion = io.hfgbarrigas.delivery.domain.api.User
                .builder()
                .id(victim.getId())
                .username(Optional.ofNullable(newData.getUsername()).orElse(victim.getUsername()))
                .password(Optional.ofNullable(newData.getPassword())
                        .map(bCryptPasswordEncoder::encode)
                        .orElse(victim.getPassword()))
                .firstName(Optional.ofNullable(newData.getFirstName()).orElse(victim.getFirstName()))
                .lastName(Optional.ofNullable(newData.getLastName()).orElse(victim.getLastName()))
                .build();

        return Optional.ofNullable(userRepository.save(Mapper.toDbUser(updatedVersion)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<io.hfgbarrigas.delivery.domain.db.User> findUserByUsername(@NotNull String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<io.hfgbarrigas.delivery.domain.db.User> findById(@NotNull Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteUser(@NotNull Long id) {
        userRepository.deleteById(id);
    }
}
