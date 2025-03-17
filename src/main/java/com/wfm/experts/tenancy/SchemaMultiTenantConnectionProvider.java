package com.wfm.experts.tenancy;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

/**
 * ✅ Provides multi-tenant database connections by switching schemas dynamically.
 */
@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider {

    private final DataSource dataSource;
    private static final String DEFAULT_SCHEMA = "public";

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
            System.err.println("❌ ERROR: Schema does not exist: " + schema + ". Falling back to default schema.");
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
            return DEFAULT_SCHEMA;  // Use default schema if tenantIdentifier is null
        }

        UUID tenantUuid;
        try {
            tenantUuid = UUID.fromString(tenantIdentifier.toString()); // Ensure valid UUID conversion
        } catch (IllegalArgumentException e) {
            System.err.println("❌ ERROR: Invalid UUID format for tenant ID `" + tenantIdentifier + "`.");
            return DEFAULT_SCHEMA; // Fallback to default schema
        }

        // ✅ Fetch schema name from the subscriptions table using the tenant ID
        String query = "SELECT tenant_schema FROM public.subscriptions WHERE tenant_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, tenantUuid); // ✅ Correctly bind UUID type
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tenant_schema"); // ✅ Return the actual schema name
                } else {
                    System.err.println("⚠️ WARNING: No schema mapping found for tenant ID `" + tenantIdentifier + "`.");
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
                    System.err.println("⚠️ WARNING: Schema `" + schema + "` not found in database.");
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
                System.out.println("✅ Current Active Schema: " + rs.getString(1));
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
