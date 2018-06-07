package io.hfgbarrigas.delivery.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.hfgbarrigas.delivery.properties.DeliveryProperties;
import io.hfgbarrigas.delivery.providers.security.UserAuthenticationProvider;
import io.hfgbarrigas.delivery.repositories.AuthorityRepository;
import io.hfgbarrigas.delivery.repositories.UserRepository;
import io.hfgbarrigas.delivery.services.Authorities;
import io.hfgbarrigas.delivery.services.Paths;
import io.hfgbarrigas.delivery.services.Users;
import io.hfgbarrigas.delivery.services.defaults.DefaultAuthoritiesService;
import io.hfgbarrigas.delivery.services.defaults.DefaultPathsService;
import io.hfgbarrigas.delivery.services.defaults.DefaultUsersService;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import java.util.Collections;

@Configuration
@EnableConfigurationProperties({DeliveryProperties.class})
public class DeliveryConfiguration {

    private DeliveryProperties deliveryProperties;

    @Autowired
    public DeliveryConfiguration(DeliveryProperties deliveryProperties) {
        this.deliveryProperties = deliveryProperties;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new JodaModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Users defaultUsersService(UserRepository userRepository,
                                     BCryptPasswordEncoder bCryptPasswordEncoder) {
        return new DefaultUsersService(userRepository, bCryptPasswordEncoder);
    }

    @Bean
    public Authorities defaultAuthoritiesService(AuthorityRepository authorityRepository,
                                                 UserRepository userRepository) {
        return new DefaultAuthoritiesService(authorityRepository, userRepository);
    }

    @Bean
    public Paths defaultPathsService(Session session) {
        return new DefaultPathsService(session);
    }

    @Bean
    public UserAuthenticationProvider userAuthenticationProvider(Users defaultUsersService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        return new UserAuthenticationProvider(defaultUsersService, bCryptPasswordEncoder);
    }

    @Bean
    public AuthenticationManager authenticationManager(UserAuthenticationProvider userAuthenticationProvider) {
        return new ProviderManager(Collections.singletonList(userAuthenticationProvider));
    }

    @Bean
    public HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository() {
        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        httpSessionCsrfTokenRepository.setHeaderName("X-CSRF-TOKEN");
        httpSessionCsrfTokenRepository.setSessionAttributeName("_csrf");
        return httpSessionCsrfTokenRepository;
    }
}
