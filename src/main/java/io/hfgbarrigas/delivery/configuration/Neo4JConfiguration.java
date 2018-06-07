package io.hfgbarrigas.delivery.configuration;

import io.hfgbarrigas.delivery.properties.Neo4JProperties;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableConfigurationProperties(Neo4JProperties.class)
@EnableNeo4jRepositories("io.hfgbarrigas.delivery.repositories")
@EnableTransactionManagement
public class Neo4JConfiguration {

    @Bean
    public SessionFactory sessionFactory(org.neo4j.ogm.config.Configuration configuration) {
        return new SessionFactory(configuration, "io.hfgbarrigas.delivery.domain.db", "BOOT-INF.classes.io.hfgbarrigas.delivery.domain.db");
    }

    @Bean
    public org.neo4j.ogm.config.Configuration configuration(Neo4JProperties neo4JProperties) {
        return new org.neo4j.ogm.config.Configuration.Builder()
                .connectionPoolSize(neo4JProperties.getConnectionPoolSize())
                .credentials(neo4JProperties.getUsername(), neo4JProperties.getPassword())
                .autoIndex(neo4JProperties.getAutoIndex())
                .connectionLivenessCheckTimeout(neo4JProperties.getConnectionLivenessCheckTimeout())
                .uri(neo4JProperties.getUri())
                .verifyConnection(neo4JProperties.isVerifyConnection())
                .build();
    }

    @Bean
    public Neo4jTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new Neo4jTransactionManager(sessionFactory);
    }
}
