package io.hfgbarrigas.delivery.providers.security;

import io.hfgbarrigas.delivery.services.Users;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.transaction.Transactional;
import java.util.Objects;

public class UserAuthenticationProvider implements AuthenticationProvider {

    private Users usersService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserAuthenticationProvider(Users usersService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.usersService = usersService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            UserDetails user = usersService.loadUserByUsername(authentication.getPrincipal().toString());
            if (Objects.nonNull(user) && bCryptPasswordEncoder.matches(authentication.getCredentials().toString(), user.getPassword())) {
                return new UsernamePasswordAuthenticationToken(user, authentication, user.getAuthorities());
            } else {
                throw new AuthenticationServiceException("Unable to authenticate.");
            }
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
