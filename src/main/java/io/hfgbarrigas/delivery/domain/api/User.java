package io.hfgbarrigas.delivery.domain.api;

import lombok.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Builder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Nullable
    private Long id;

    @NotNull
    private String username;

    @NotNull
    private String password;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private Set<Authority> authorities;
}
