package io.hfgbarrigas.delivery.api;

import com.google.common.base.Strings;
import io.hfgbarrigas.delivery.domain.api.ErrorDetails;
import io.hfgbarrigas.delivery.domain.api.User;
import io.hfgbarrigas.delivery.exceptions.DuplicateUserException;
import io.hfgbarrigas.delivery.exceptions.InvalidDataException;
import io.hfgbarrigas.delivery.exceptions.UnexpectedException;
import io.hfgbarrigas.delivery.exceptions.UnknownUserException;
import io.hfgbarrigas.delivery.services.Authorities;
import io.hfgbarrigas.delivery.services.Users;
import io.hfgbarrigas.delivery.utils.Loggable;
import io.hfgbarrigas.delivery.utils.Mapper;
import io.hfgbarrigas.delivery.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RepositoryRestController
public class UsersApi implements Loggable {

    private Authorities authorities;
    private Users users;

    @Autowired
    public UsersApi(Authorities authorities, Users users) {
        this.authorities = authorities;
        this.users = users;
    }

    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<User>> saveUser(@Valid @RequestBody User user) {

        //spring data rest and validation don't play nice together, unfortunately I have to validate this by hand
        if (Strings.isNullOrEmpty(user.getUsername()) || Strings.isNullOrEmpty(user.getPassword())) {
            throw new InvalidDataException("Supply username and password.");
        }

        io.hfgbarrigas.delivery.domain.db.User created = this.users.saveUser(user)
                .orElseThrow(() -> new UnexpectedException("Something went wrong creating the user."));

        return CompletableFuture.completedFuture(ResponseEntity
                .created(URI.create(String.format("/users/%s", created.getId())))
                .body(Mapper.toApiUser(created))); //actually blocking netty threads here.
    }

    @PutMapping(value = "/users/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<User>> updateUser(@NotNull @PathVariable Long id, @Valid @RequestBody User user) {

        io.hfgbarrigas.delivery.domain.db.User updated = this.users.updateUser(id, user)
                .orElseThrow(() -> new UnexpectedException("Something went wrong updating the user."));

        return CompletableFuture.completedFuture(ResponseEntity.ok(Mapper.toApiUser(updated)));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "/users/{id}/authorities", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<Void>> addUserAuthorities(@PathVariable("id") Long id, @NotEmpty @RequestBody List<String> authorities) {

        if (ValidationUtils.isNullOrEmpty(authorities)) {
            throw new InvalidDataException("Authorities are required.");
        }

        this.authorities.addUserAuthorities(id, authorities);

        return CompletableFuture.completedFuture(ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping(value = "/users/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteUser(@PathVariable("id") Long id) {
        this.users.deleteUser(id);
        return CompletableFuture.completedFuture(ResponseEntity.noContent().build());
    }

    @ExceptionHandler(UnknownUserException.class)
    public final ResponseEntity<ErrorDetails> error(UnknownUserException ex, HttpServletRequest request) {
        return buildError(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public final ResponseEntity<ErrorDetails> error(DuplicateUserException ex, HttpServletRequest request) {
        return buildError(ex, request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidDataException.class)
    public final ResponseEntity<ErrorDetails> error(InvalidDataException ex, HttpServletRequest request) {
        return buildError(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ErrorDetails> error(AccessDeniedException ex, HttpServletRequest request) {
        return buildError(ex, request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> error(Exception ex, HttpServletRequest request) {
        return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorDetails> buildError(Exception ex, HttpServletRequest request, HttpStatus status) {
        this.logger().error("Error: ", ex);
        return ResponseEntity
                .status(status)
                .body(ErrorDetails.builder()
                        .timestamp(OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))
                        .exception(ex.getClass().getSimpleName())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .build());
    }
}
