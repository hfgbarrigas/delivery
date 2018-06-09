package io.hfgbarrigas.delivery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.hfgbarrigas.delivery.domain.api.Authority;
import io.hfgbarrigas.delivery.domain.api.Place;
import io.hfgbarrigas.delivery.domain.api.User;
import io.hfgbarrigas.delivery.domain.db.Route;
import io.hfgbarrigas.delivery.exceptions.DatasourceInitializationException;
import io.hfgbarrigas.delivery.repositories.AuthorityRepository;
import io.hfgbarrigas.delivery.repositories.PlaceRepository;
import io.hfgbarrigas.delivery.repositories.RouteRepository;
import io.hfgbarrigas.delivery.repositories.UserRepository;
import io.hfgbarrigas.delivery.services.Authorities;
import io.hfgbarrigas.delivery.services.Users;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class DeliveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(@NotNull AuthorityRepository authorityRepository,
                                          @NotNull UserRepository userRepository,
                                          @NotNull Users usersService,
                                          @NotNull Authorities authoritiesService,
                                          @NotNull ApplicationContext ctx,
                                          @NotNull ObjectMapper objectMapper,
                                          @NotNull PlaceRepository placeRepository,
                                          @NotNull RouteRepository routeRepository) {

        final List<User> users;
        try {
            final Map<String, List<User>> staticData = objectMapper.readValue(ctx.getResource("classpath:data/users.json").getInputStream(), new TypeReference<Map<String, List<User>>>() {
            });
            users = new ArrayList<>(staticData.get("users"));
        } catch (Exception e) {
            throw new DatasourceInitializationException(e);
        }

        final List<String> authorities;
        try {
            final Map<String, List<String>> staticData = objectMapper.readValue(ctx.getResource("classpath:data/authorities.json").getInputStream(), new TypeReference<Map<String, List<String>>>() {
            });
            authorities = new ArrayList<>(staticData.get("authorities"));
        } catch (Exception e) {
            throw new DatasourceInitializationException(e);
        }

        final List<Place> places;
        try {
            final Map<String, List<Place>> staticData = objectMapper.readValue(ctx.getResource("classpath:data/places.json").getInputStream(), new TypeReference<Map<String, List<Place>>>() {
            });
            places = new ArrayList<>(staticData.get("places"));
        } catch (Exception e) {
            throw new DatasourceInitializationException(e);
        }

        return args -> {
            //authorities are a restricted resource, we need to simulate a user with admin authorities
            try {
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                SecurityContextHolder.setContext(securityContext);
                UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "test", AuthorityUtils.createAuthorityList("USER", "ADMIN"));

                securityContext.setAuthentication(new AnonymousAuthenticationToken("dummy", userDetails, userDetails.getAuthorities()));

                //clear
                authorityRepository.deleteAll();
                userRepository.deleteAll();
                placeRepository.deleteAll();
                routeRepository.deleteAll();

                //create users
                List<Optional<io.hfgbarrigas.delivery.domain.db.User>> created = users.stream().map(usersService::saveUser).collect(toList());

                //create authorities
                authoritiesService.createAuthorities(authorities);

                Map<String, io.hfgbarrigas.delivery.domain.db.User> createdMap = created.stream()
                        .map(Optional::get)
                        .collect(toMap(io.hfgbarrigas.delivery.domain.db.User::getUsername, Function.identity()));

                //add users authorities
                users.forEach(u -> authoritiesService.addUserAuthorities(createdMap.get(u.getUsername()).getId(), Objects.requireNonNull(u.getAuthorities())
                        .stream()
                        .map(Authority::getName)
                        .collect(toList())));

                //add places
                Map<String, io.hfgbarrigas.delivery.domain.db.Place> createdPlaces = Lists.newArrayList(placeRepository.save(places.stream()
                        .map(p -> io.hfgbarrigas.delivery.domain.db.Place.builder()
                                .name(p.getName())
                                .build())
                        .collect(toList()), 0))
                        .stream()
                        .collect(toMap(io.hfgbarrigas.delivery.domain.db.Place::getName, Function.identity()));

                //add routes
                places.forEach(p -> routeRepository.save(p.getRoutes().stream()
                        .map(r -> Route.builder()
                                .cost(r.getCost())
                                .time(r.getTime())
                                .destination(io.hfgbarrigas.delivery.domain.db.Place
                                        .builder()
                                        .id(createdPlaces.get(r.getDestination().getName()).getId())
                                        .name(r.getDestination().getName())
                                        .build())
                                .start(io.hfgbarrigas.delivery.domain.db.Place
                                        .builder()
                                        .id(createdPlaces.get(r.getStart().getName()).getId())
                                        .name(r.getStart().getName())
                                        .build())
                                .build())
                        .collect(toSet()), 0));

            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }
}
