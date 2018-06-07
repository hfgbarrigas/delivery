package io.hfgbarrigas.delivery.api;

import io.hfgbarrigas.delivery.domain.api.ErrorDetails;
import io.hfgbarrigas.delivery.services.Authorities;
import io.hfgbarrigas.delivery.utils.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RepositoryRestController
public class AuthoritiesApi implements Loggable {

    private Authorities authorities;

    @Autowired
    public AuthoritiesApi(Authorities authorities) {
        this.authorities = authorities;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(value = "/authorities", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @Validated
    public CompletableFuture<ResponseEntity<Void>> addAuthorities(@NotEmpty @RequestBody List<String> authorities) {
        this.authorities.createAuthorities(authorities);
        return CompletableFuture.completedFuture(ResponseEntity.created(URI.create("/authorities")).build());
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> error(Exception ex, HttpServletRequest request) {
        this.logger().error("Error: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDetails.builder()
                        .timestamp(OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))
                        .exception(ex.getClass().getSimpleName())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .build());
    }
}
