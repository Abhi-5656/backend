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

@Service
public class TenantServiceImpl implements TenantService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TenantServiceImpl(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Object> createTenantSchema(String companyName) throws Exception {
        // ✅ Generate a **secure** UUID-based Tenant ID (Internal Use)
        UUID tenantId = UUID.randomUUID();  // 🔥 Use `UUID` instead of `String`

        // ✅ Convert Company Name to a **Valid Schema Name** (Database Use)
        String tenantSchema = convertCompanyNameToSchema(companyName);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // ✅ **Create schema only if it does not exist**
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + tenantSchema);
        }

//         ✅ **Run Flyway migration** for the new schema
        runFlywayMigration(tenantSchema);

        // ✅ **Return Tenant Details**
        Map<String, Object> tenantDetails = new HashMap<>();
        tenantDetails.put("tenantId", tenantId);  // 🔒 Store as `UUID`, not `String`
        tenantDetails.put("tenantSchema", tenantSchema);  // 🔐 Internal schema mapping
        tenantDetails.put("tenantUrl", generateTenantUrl(companyName));  // 🔒 Secure **Company-Based** Tenant URL

        return tenantDetails;
    }



    /**
     * ✅ **Runs Flyway migrations** for the given schema.
     * @param schemaName The schema where migrations should be applied.
     */
    private void runFlywayMigration(String schemaName) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)  // ✅ Apply migrations **only to this schema**
                .locations("classpath:db/migration/tenants")  // ✅ **Tenant-specific migrations**
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
    }

    /**
     * ✅ **Generates a Tenant-Specific URL using the Company Name**
     * Example: `"XYZ Pvt Ltd"` → `http://localhost:8080/tenants/xyz-pvt-ltd`
     * @param companyName The original company name.
     * @return The **Company-Based** Tenant URL.
     */
    private String generateTenantUrl(String companyName) {
        String baseUrl = "http://localhost:8080";  // ✅ Base URL (**Change in production**)
        return baseUrl + "/tenants/" + convertCompanyNameToUrl(companyName);  // 🔒 Secure **Company-Based** URL
    }

    /**
     * ✅ **Converts Company Name to a Valid Schema Name for DB**
     * Example: `"XYZ Pvt Ltd"` → `"xyz_pvt_ltd"`
     */
    private String convertCompanyNameToSchema(String companyName) {
        return companyName.trim()
                .replaceAll("[^a-zA-Z0-9]", "_")  // Replace special characters with `_`
                .toLowerCase();
    }

    /**
     * ✅ **Converts Company Name to a Valid URL Format**
     * Example: `"XYZ Pvt Ltd"` → `"xyz-pvt-ltd"`
     */
    private String convertCompanyNameToUrl(String companyName) {
        return companyName.trim()
                .replaceAll("[^a-zA-Z0-9]", "-")  // Replace special characters with `-`
                .toLowerCase();
    }
}
