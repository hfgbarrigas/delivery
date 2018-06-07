package io.hfgbarrigas.delivery.domain.api;

import lombok.*;

import java.util.List;

@Builder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    private String name;
    private List<Route> routes;
}
