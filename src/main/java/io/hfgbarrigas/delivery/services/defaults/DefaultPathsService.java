package io.hfgbarrigas.delivery.services.defaults;

import com.google.common.collect.ImmutableMap;
import io.hfgbarrigas.delivery.domain.db.Path;
import io.hfgbarrigas.delivery.domain.db.Route;
import io.hfgbarrigas.delivery.services.Paths;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Stream;

public class DefaultPathsService implements Paths {

    private static final String START = "start";
    private static final String END = "end";
    private static final String TIME = "time";
    private static final String COST = "cost";
    private static final String RELATIONSHIPS = "relationships";
    private static final String EXTRA_CONDITIONS = "[EXTRA_CONDITIONS]";
    private static final String ALGORITHM = "[ALGORITHM]";
    private static final String SHORTEST_PATH = "shortestPath";
    private static final String ALL_SHORTEST_PATHS = "allShortestPaths";

    private static final String query = String.format("MATCH path=%s((start:Place {name: {start}})-[:Route*]->(end:Place {name: {end}}))\n", ALGORITHM) +
            "WITH path, " +
            "REDUCE(cost = 0, rel in relationships(path) | cost + rel.cost) AS cost," +
            "REDUCE(time = 0, rel in relationships(path) | time + rel.time) AS time\n" +
            String.format("WHERE length(path) > 1 %s\n", EXTRA_CONDITIONS) +
            "RETURN nodes(path) as nodes, relationships(path) as relationships, cost, time\n" +
            "ORDER BY time";

    private Session session;

    public DefaultPathsService(Session session) {
        this.session = session;
    }


    @Override
    public List<Path> getPaths(@NotNull String start, @NotNull String end) {
        return null;
    }

    @Override
    public List<Path> getPaths(@NotNull String start, @NotNull String end, @Nullable Integer time) {
        return null;
    }

    @Override
    public List<Path> getPaths(@NotNull String start, @NotNull String end, @Nullable Integer time, @Nullable Integer cost) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Path> getPaths(@NotNull String start,
                               @NotNull String end,
                               @Nullable Integer time,
                               @Nullable Integer cost,
                               @Nullable String algorithm) {

        StringBuilder builder = new StringBuilder("");

        Optional.ofNullable(time).ifPresent(t -> builder.append(String.format(" AND %s <= %s", TIME, time)));
        Optional.ofNullable(cost).ifPresent(c -> builder.append(String.format(" AND %s <= %s", COST, cost)));

        return parseResult(session.query(query
                .replace(ALGORITHM, getAlg(algorithm))
                .replace(EXTRA_CONDITIONS, builder.toString()), ImmutableMap.of(START, start, END, end), true));
    }

    @SuppressWarnings("unchecked")
    private List<Path> parseResult(@NotNull Result result) {
        Iterable<Map<String, Object>> results = result.queryResults();
        List<Path> col = new ArrayList<>();
        for (Map<String, Object> res : results) { //ignoring cast protection because result itself contains the actual types already converted
            Long time = (Long) res.getOrDefault(TIME, 0);
            Long cost = (Long) res.getOrDefault(COST, 0);
            List<Route> path = (List<Route>) res.getOrDefault(RELATIONSHIPS, Collections.emptySet());

            col.add(Path.builder()
                    .cost(cost)
                    .path(path)
                    .time(time)
                    .build());
        }
        return col;
    }

    private String getAlg(@Nullable String alg) {
        return Optional.ofNullable(alg)
                .map(a -> Stream.of("", "shortestPath", "allShortestPaths")
                        .filter(eligible -> eligible.equals(a))
                        .findFirst()
                        .orElse(""))
                .orElse("");
    }
}
