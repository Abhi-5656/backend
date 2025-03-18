package com.wfm.experts.service.impl;

import com.wfm.experts.service.TenantService;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * ✅ Service for multi-tenant schema management.
 * ✅ Handles schema creation, Flyway migrations, and tenant onboarding.
 */
@Service
public class TenantServiceImpl implements TenantService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private static final Logger LOGGER = Logger.getLogger(TenantServiceImpl.class.getName());

    @Autowired
    public TenantServiceImpl(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * ✅ Creates a new tenant schema and returns the tenant details.
     *
     * @param companyName The company name for the new tenant.
     * @return A map containing `tenantId` and `tenantSchema` details.
     * @throws Exception If schema creation fails.
     */
    @Override
    public Map<String, Object> createTenantSchema(String companyName) throws Exception {
        // ✅ Generate a unique UUID-based Tenant ID
        UUID tenantId = UUID.randomUUID();

        // ✅ Convert company name to a valid schema name
        String tenantSchema = convertCompanyNameToSchema(companyName);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // ✅ Create schema if it does not exist
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + tenantSchema);
        }

        // ✅ Run Flyway migrations for the new schema
        runFlywayMigration(tenantSchema);

        // ✅ Return Tenant Details
        Map<String, Object> tenantDetails = new HashMap<>();
        tenantDetails.put("tenantId", tenantId);
        tenantDetails.put("tenantSchema", tenantSchema);

        LOGGER.info("✅ New Tenant Created - ID: " + tenantId + ", Schema: " + tenantSchema);

        return tenantDetails;
    }

    /**
     * ✅ Runs Flyway database migrations for the newly created schema.
     *
     * @param schemaName The schema where migrations should be applied.
     */
    private void runFlywayMigration(String schemaName) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)  // ✅ Apply migrations **only to this schema**
                .locations("classpath:db/migration/tenants")  // ✅ Tenant-specific migrations
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
        LOGGER.info("✅ Flyway Migration Completed for Schema: " + schemaName);
    }

    /**
     * ✅ Converts Company Name to a Valid Schema Name.
     * Example: "XYZ Pvt Ltd" → "xyz_pvt_ltd"
     *
     * @param companyName The company name.
     * @return The schema-safe company name.
     */
    private String convertCompanyNameToSchema(String companyName) {
        return companyName.trim()
                .replaceAll("[^a-zA-Z0-9]", "_")  // ✅ Replace special characters with `_`
                .toLowerCase();
    }
}
