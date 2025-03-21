package com.wfm.experts.service.impl;

import com.wfm.experts.service.TenantService;
import com.wfm.experts.util.TenantIdUtil;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ✅ Service for multi-tenant schema management.
 */
@Service
public class TenantServiceImpl implements TenantService {

    private final DataSource dataSource;
    private static final Logger LOGGER = Logger.getLogger(TenantServiceImpl.class.getName());

    @Autowired
    public TenantServiceImpl(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
    }

    /**
     * ✅ Creates a new tenant schema and returns the tenant details.
     */
    @Override
    public Map<String, Object> createTenantSchema(String companyName) throws Exception {

        // ✅ Convert `tenantId` to schema name using `TenantIdUtil`
        String tenantSchema = TenantIdUtil.convertCompanyNameToSchema(companyName);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // ✅ Create schema if it does not exist
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + tenantSchema);
        }

        // ✅ Run Flyway migrations for the new schema
        runFlywayMigration(tenantSchema);

        // ✅ Return Tenant Details
        Map<String, Object> tenantDetails = new HashMap<>();
        tenantDetails.put("tenantSchema", tenantSchema);

        LOGGER.info("✅ New Tenant Created - Schema: " + tenantSchema);
        return tenantDetails;
    }

    /**
     * ✅ Runs Flyway database migrations for the newly created schema.
     */
    private void runFlywayMigration(String schemaName) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/tenants")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
        LOGGER.info("✅ Flyway Migration Completed for Schema: " + schemaName);
    }
}
