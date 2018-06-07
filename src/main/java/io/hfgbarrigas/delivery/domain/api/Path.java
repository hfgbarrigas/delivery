package io.hfgbarrigas.delivery.domain.api;

import lombok.*;

import java.util.List;

@Builder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Path {
    private Long time;
    private Long cost;
    List<Route> path;
}
