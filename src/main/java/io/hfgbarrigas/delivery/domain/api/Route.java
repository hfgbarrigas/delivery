package io.hfgbarrigas.delivery.domain.api;

import lombok.*;

@Builder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    private Long id;
    private Integer time;
    private Integer cost;
    private Place destination;
    private Place start;

}
