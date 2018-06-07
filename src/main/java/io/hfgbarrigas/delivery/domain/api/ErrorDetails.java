package io.hfgbarrigas.delivery.domain.api;

import lombok.*;

import java.time.OffsetDateTime;

@Builder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {

    private OffsetDateTime timestamp;
    private Integer status;
    private String error;
    private String exception;
    private String message;
    private String path;

}
