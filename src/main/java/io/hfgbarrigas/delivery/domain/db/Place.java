package io.hfgbarrigas.delivery.domain.db;

import lombok.*;
import org.neo4j.ogm.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
@Builder
@ToString(of = {"name", "id"})
@EqualsAndHashCode(of = {"name", "id"})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Index(unique = true)
    private String name;

    @Relationship(type = "Route")
    private Set<Route> routes = new HashSet<>();
}
