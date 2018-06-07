package io.hfgbarrigas.delivery.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hfgbarrigas.delivery.domain.api.ErrorDetails;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Configuration
@EnableWebSecurity
@Order(1)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SecurityConfiguration.class);
    private static final String REQUEST_ATTRIBUTE_NAME = "_csrf";
    private static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";
    private static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";
    private static final String RESPONSE_TOKEN_NAME = "X-CSRF-TOKEN";

    private AuthenticationManager authenticationManager;
    private ObjectMapper objectMapper;
    private HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository;

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(HttpMethod.POST, "/users");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/management/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .successHandler((request, response, authentication) -> {
                    CsrfToken token = (CsrfToken) request.getAttribute(REQUEST_ATTRIBUTE_NAME);

                    if (token != null) {
                        response.setHeader(RESPONSE_HEADER_NAME, token.getHeaderName());
                        response.setHeader(RESPONSE_PARAM_NAME, token.getParameterName());
                        response.setHeader(RESPONSE_TOKEN_NAME, token.getToken());
                    }

                    response.setStatus(HttpServletResponse.SC_OK);
                })
                .failureHandler((request, response, exception) -> buildExceptionResponse(request, response, exception, 401))
                .loginPage("/login").permitAll()
                .and()
                .logout()
                .deleteCookies()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutUrl("/logout").permitAll()
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .and()
                .csrf()
                .ignoringAntMatchers("/login", "/management/**")
                .requireCsrfProtectionMatcher(request -> !HttpMethod.OPTIONS.toString().equals(request.getMethod()))
                .csrfTokenRepository(httpSessionCsrfTokenRepository)
                .and()
                .exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) -> buildExceptionResponse(request, response, accessDeniedException, 403))
                .defaultAuthenticationEntryPointFor((request, response, exception) -> buildExceptionResponse(request, response, exception, 401), new AntPathRequestMatcher("/**"));

        http.sessionManagement()
                //.maximumSessions(1)
                //.maxSessionsPreventsLogin(true)
                //.and()
                .sessionAuthenticationFailureHandler((request, response, exception) -> buildExceptionResponse(request, response, exception, 401))
                .and()
                .sessionManagement()
                .sessionFixation()
                .newSession();
    }

    private void buildExceptionResponse(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Exception exception,
                                        int status) throws IOException {
        if (Objects.nonNull(exception)) {
            LOGGER.error("Authentication/Authorization error.", exception);
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status);

        //clear the cookie
        Cookie cookie = new Cookie("x-delivery-auth", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        response.getOutputStream()
                .println(objectMapper
                        .writeValueAsString(ErrorDetails.builder()
                                .timestamp(OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))
                                .exception(exception.getClass().getSimpleName())
                                .message(exception.getMessage())
                                .path(request.getRequestURI())
                                .status(status)
                                .error(HttpStatus.valueOf(status).getReasonPhrase())
                                .build()));
    }

    @Override
    protected AuthenticationManager authenticationManager() {
        return authenticationManager;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setHttpSessionCsrfTokenRepository(HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository) {
        this.httpSessionCsrfTokenRepository = httpSessionCsrfTokenRepository;
    }
}
