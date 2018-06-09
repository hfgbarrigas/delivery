package io.hfgbarrigas.delivery.domain.db;

import lombok.*;
import org.neo4j.ogm.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Set;

@NodeEntity
@Builder
@ToString(of = {"name", "id"})
@EqualsAndHashCode(of = {"name", "id"})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Authority {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Index(unique = true)
    private String name;

    @Relationship(type = "HAS", direction = Relationship.UNDIRECTED)
    private Set<User> users;

}
