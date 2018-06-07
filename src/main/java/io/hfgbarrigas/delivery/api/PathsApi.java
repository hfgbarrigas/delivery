package io.hfgbarrigas.delivery.api;

import io.hfgbarrigas.delivery.domain.api.Algorithm;
import io.hfgbarrigas.delivery.domain.api.ErrorDetails;
import io.hfgbarrigas.delivery.domain.api.Path;
import io.hfgbarrigas.delivery.services.Paths;
import io.hfgbarrigas.delivery.utils.Loggable;
import io.hfgbarrigas.delivery.utils.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

@RestController
public class PathsApi implements Loggable {

    private Paths paths;

    @Autowired
    public PathsApi(Paths paths) {
        this.paths = paths;
    }

    @RequestMapping(value = "/paths", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public CompletableFuture<ResponseEntity<List<Path>>> allShortestCapped(@RequestParam String start,
                                                                           @RequestParam String end,
                                                                           @RequestParam(required = false) Integer time,
                                                                           @RequestParam(required = false) Integer cost,
                                                                           @RequestParam(required = false) Algorithm algorithm) {
        return CompletableFuture.completedFuture(ResponseEntity.ok()
                .body(paths.getPaths(start, end, time, cost, Optional.ofNullable(algorithm)
                        .map(Algorithm::getAlg)
                        .orElse(""))
                        .stream().map(Mapper::toApiPath).collect(toList())));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public final ResponseEntity<ErrorDetails> error(MissingServletRequestParameterException ex, HttpServletRequest request) {
        this.logger().error("Error: ", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorDetails.builder()
                        .timestamp(OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))
                        .exception(ex.getClass().getSimpleName())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .build());
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
