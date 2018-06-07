package io.hfgbarrigas.delivery.repositories;

import io.hfgbarrigas.delivery.domain.db.Path;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

//@RepositoryRestResource(path = "paths", collectionResourceRel = "paths")
//@RestResource(exported = false)
public interface PathRepository extends Neo4jRepository<Path, Long> {

    //limit is recommend for performance reasons. Limiting to 10, paramenter limit is not yet supported
    //@RestResource
    @Query("MATCH path=shortestPath((start:Place {name: {start}})-[:Route*1..10]->(end:Place {name: {end}}))\n" +
            "WITH path, " +
            "REDUCE(cost = 0, rel in relationships(path) | cost + rel.cost) AS cost," +
            "REDUCE(time = 0, rel in relationships(path) | time + rel.time) AS time\n" +
            "WHERE length(path) > 1\n" +
            "RETURN nodes(path) as nodes, relationships(path) as relationships, cost, time")
    Path shortest(@Param("start") String start, @Param("end") String end);

    //@RestResource
    @Query("MATCH path=allShortestPaths((start:Place {name: {start}})-[:Route*1..10]->(end:Place {name: {end}}))\n" +
            "WITH path, " +
            "REDUCE(cost = 0, rel in relationships(path) | cost + rel.cost) AS cost," +
            "toInteger(rand() * 10000) AS id," +
            "REDUCE(time = 0, rel in relationships(path) | time + rel.time) AS time\n" +
            "WHERE length(path) > 1 and time <= {timeCap} \n" +
            "RETURN nodes(path) as nodes, relationships(path) as relationships, cost, time, id")
    Iterable<Path> allShortestTimeCapped(@Param("start") String start, @Param("end") String end, @Param("timeCap") Integer timeCap);

    //@RestResource
    @Query("MATCH path=allShortestPaths((start:Place {name: {start}})-[:Route*1..10]->(end:Place {name: {end}}))\n" +
            "WITH path, " +
            "REDUCE(cost = 0, rel in relationships(path) | cost + rel.cost) AS cost," +
            "REDUCE(time = 0, rel in relationships(path) | time + rel.time) AS time\n" +
            "WHERE length(path) > 1 and cost <= {costCap} \n" +
            "RETURN nodes(path) as nodes, relationships(path) as relationships, cost, time")
    List<Path> allShortestCostCapped(@Param("start") String start, @Param("end") String end, @Param("costCap") Integer costCap);
}
