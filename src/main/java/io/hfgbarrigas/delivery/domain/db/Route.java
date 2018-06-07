package io.hfgbarrigas.delivery.domain.db;

import lombok.*;
import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "Route")
@Builder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue
    private Long id;

    @Property
    private Integer time;

    @Property
    private Integer cost;

    @StartNode
    private Place start;

    @EndNode
    private Place destination;

}
