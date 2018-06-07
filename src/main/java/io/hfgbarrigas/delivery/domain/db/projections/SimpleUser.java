package io.hfgbarrigas.delivery.domain.db.projections;

import io.hfgbarrigas.delivery.domain.db.User;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "user", types = {User.class})
public interface SimpleUser {
    Long getId();
    String getUsername();
    String getFirstName();
    String getLastName();
    Long getCreatedDate();
}
