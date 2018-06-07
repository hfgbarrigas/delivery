package io.hfgbarrigas.delivery.services.defaults;

import com.google.common.collect.Lists;
import io.hfgbarrigas.delivery.domain.db.Authority;
import io.hfgbarrigas.delivery.domain.db.User;
import io.hfgbarrigas.delivery.exceptions.DuplicateAuthorityException;
import io.hfgbarrigas.delivery.exceptions.UnknownUserException;
import io.hfgbarrigas.delivery.repositories.AuthorityRepository;
import io.hfgbarrigas.delivery.repositories.UserRepository;
import io.hfgbarrigas.delivery.services.Authorities;
import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class DefaultAuthoritiesService implements Authorities {

    private AuthorityRepository authorityRepository;
    private UserRepository userRepository;

    public DefaultAuthoritiesService(AuthorityRepository authorityRepository,
                                     UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }

    @Override
    @Transactional
    public Collection<Authority> addUserAuthorities(@NonNull Long id, @NotEmpty List<String> authorities) {
        List<Authority> r = authorityRepository.findAllByNameIn(authorities);
        Optional<User> user = userRepository.findById(id);

        if(!user.isPresent()) {
            throw new UnknownUserException();
        }
        User user1 = user.get();

        if (user1.getAuthorities() == null) {
            user1 = User.builder()
                    .createdDate(user1.getCreatedDate())
                    .firstName(user1.getFirstName())
                    .lastName(user1.getLastName())
                    .password(user1.getPassword())
                    .username(user1.getUsername())
                    .id(user1.getId())
                    .authorities(new HashSet<>(r))
                    .build();
        } else {
            user1.getAuthorities().addAll(r);
        }
        return Optional.ofNullable(userRepository.save(user1).getAuthorities())
                .orElseGet(Collections::emptySet);
    }

    @Override
    @Transactional
    public Optional<Authority> addUserAuthority(@NonNull Long id, @NonNull String authority) {
        return this.addUserAuthorities(id, Collections.singletonList(authority))
                .stream()
                .findFirst();
    }

    @Override
    @Transactional
    public Collection<Authority> createAuthorities(@NonNull @NotEmpty @Valid List<String> authorities) {
        List<String> existing = authorityRepository.findAllByNameIn(authorities)
                .stream()
                .map(Authority::getName)
                .collect(toList());

        if (!existing.isEmpty()) {
            authorities = authorities.stream()
                    .filter(existing::contains)
                    .collect(toList());
        }

        return Lists.newArrayList(authorityRepository.saveAll(authorities
                .stream()
                .map(a -> Authority.builder().name(a).build())
                .collect(toList())));
    }

    @Override
    @Transactional
    public Optional<Authority> createAuthority(@NonNull String authority) {
        Authority auth = authorityRepository.findByNameEquals(authority);

        if (auth != null) {
            throw new DuplicateAuthorityException(String.format("Authority %s already exists.", authority));
        }

        return Optional.ofNullable(authorityRepository.save(Authority.builder().name(authority).build()));
    }
}
