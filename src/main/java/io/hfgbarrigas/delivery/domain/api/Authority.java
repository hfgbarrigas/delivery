package io.hfgbarrigas.delivery.domain.api;

import lombok.*;

@Builder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Authority {
    private Long id;
    private String name;
}
