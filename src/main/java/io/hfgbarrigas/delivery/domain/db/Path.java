package io.hfgbarrigas.delivery.domain.db;

import lombok.*;

import java.util.List;

@Builder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
//@QueryResult
public class Path {

//    @Id
//    @GeneratedValue
//    private Long id;

    /**
     * There's bug for spring data rest and neo4j @QueryResult.
     * Creating a repository of type Path (check PathRepository), we encounter the error java.util.LinkedHashMap cannot be cast to Path.
     * We are forced to use a NodeEntity (e.g: Route) in order for it to work, however, when using collections as return type
     * hateoas enforces the usage of ID. Even creating a dummy ID for a @QueryResult, it doesn't make a lot of sense because it is not
     * a resource discoverable, we encounter a serialization exception (Type null is not of type Serializable) because it cannot find
     * the annotation @Id in @QueryResult types.
     */

    private Long time;
    private Long cost;
    List<Route> path;
}
