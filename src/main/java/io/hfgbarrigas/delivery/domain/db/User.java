package io.hfgbarrigas.delivery.domain.db;

import lombok.*;
import org.neo4j.ogm.annotation.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Set;

@NodeEntity
@Builder
@ToString(of = {"username", "id"})
@EqualsAndHashCode(of = {"username", "id"})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Index(unique = true)
    private String username;

    @NotNull
    private String password;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @NotNull
    private Long createdDate;

    @Relationship(type = "HAS", direction = Relationship.UNDIRECTED)
    private Set<Authority> authorities;
}
