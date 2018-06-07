package io.hfgbarrigas.delivery.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "neo4j")
public class Neo4JProperties {
    private int connectionPoolSize = 10;
    private String username;
    private String password;
    private String autoIndex = "update";
    private int connectionLivenessCheckTimeout;
    private String uri = "bolt://localhost";
    private boolean verifyConnection = true;

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public void setConnectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAutoIndex() {
        return autoIndex;
    }

    public void setAutoIndex(String autoIndex) {
        this.autoIndex = autoIndex;
    }

    public int getConnectionLivenessCheckTimeout() {
        return connectionLivenessCheckTimeout;
    }

    public void setConnectionLivenessCheckTimeout(int connectionLivenessCheckTimeout) {
        this.connectionLivenessCheckTimeout = connectionLivenessCheckTimeout;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isVerifyConnection() {
        return verifyConnection;
    }

    public void setVerifyConnection(boolean verifyConnection) {
        this.verifyConnection = verifyConnection;
    }
}
