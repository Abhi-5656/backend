package com.wfm.experts.tenancy;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * ✅ Provides multi-tenant database connections by dynamically switching schemas.
 * ✅ Uses `String tenantId` for secure schema resolution.
 */
@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider {

    private final DataSource dataSource;
    private static final String DEFAULT_SCHEMA = "public";
    private static final Logger LOGGER = Logger.getLogger(SchemaMultiTenantConnectionProvider.class.getName());

    // ✅ Cache to store tenantId → schema mappings for better performance
    private static final Map<String, String> tenantSchemaCache = new ConcurrentHashMap<>();

    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * ✅ Gets a connection for the specific tenant schema.
     */
    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        final Connection connection = dataSource.getConnection();

        // ✅ Resolve schema name from tenant ID
        String schema = resolveSchemaName(connection, tenantIdentifier);

        // ✅ Check if schema exists before switching
        if (!schemaExists(connection, schema)) {
            LOGGER.warning("❌ Schema does not exist: " + schema + ". Falling back to default schema.");
            schema = DEFAULT_SCHEMA;
        }

        // ✅ Switch to the correct schema
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO " + schema);
        }

        // ✅ Log the active schema
        logCurrentSchema(connection);

        return connection;
    }

    /**
     * ✅ Resolves the schema name from the tenant ID.
     */
    private String resolveSchemaName(Connection connection, Object tenantIdentifier) throws SQLException {
        if (tenantIdentifier == null) {
            return DEFAULT_SCHEMA;  // ✅ Use default schema if tenantIdentifier is null
        }

        String tenantId = tenantIdentifier.toString(); // ✅ Handle tenantId as String

        // ✅ Check Cache Before Querying DB
        if (tenantSchemaCache.containsKey(tenantId)) {
            return tenantSchemaCache.get(tenantId);
        }

        // ✅ Fetch schema name from the subscriptions table using the tenant ID
        String query = "SELECT tenant_schema FROM public.subscriptions WHERE tenant_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tenantId); // ✅ Correctly bind String type
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String schema = rs.getString("tenant_schema");
                    tenantSchemaCache.put(tenantId, schema);  // ✅ Cache it for future use
                    return schema;
                } else {
                    LOGGER.warning("⚠️ No schema mapping found for tenant ID `" + tenantId + "`.");
                    return DEFAULT_SCHEMA;
                }
            }
        }
    }

    /**
     * ✅ Checks if the given schema exists in the database.
     */
    private boolean schemaExists(Connection connection, String schema) throws SQLException {
        String query = "SELECT schema_name FROM information_schema.schemata WHERE schema_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                } else {
                    LOGGER.warning("⚠️ Schema `" + schema + "` not found in database.");
                    return false;
                }
            }
        }
    }

    /**
     * ✅ Logs the current schema to verify that switching was successful.
     */
    private void logCurrentSchema(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW search_path")) {
            if (rs.next()) {
                LOGGER.info("✅ Current Active Schema: " + rs.getString(1));
            }
        }
    }

    /**
     * ✅ Releases the connection back to the pool.
     */
    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * ✅ Gets a generic connection with the default schema.
     */
    @Override
    public Connection getAnyConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO " + DEFAULT_SCHEMA);
        }
        return connection;
    }

    /**
     * ✅ Releases a generic connection.
     */
    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * ✅ Returns false to indicate Hibernate should manage aggressive connection release.
     */
    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
