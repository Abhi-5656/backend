package com.wfm.experts.util;

import java.util.Locale;

public class TenantIdUtil {

    private TenantIdUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * ✅ Generates a unique `tenantId` by transforming `companyName` into a URL-safe format.
     * Example:
     *   - "NextGen Solutions Inc." → "nextgen-solutions-inc"
     *   - "ACME Corp & Sons" → "acme-corp-sons"
     *   - "Hello@World_123!" → "hello-world-123"
     *
     * @param companyName The company name to convert.
     * @return A sanitized, URL-safe `tenantId`.
     */
    public static String generateTenantId(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty when generating tenantId.");
        }
        return companyName
                .toLowerCase(Locale.ROOT)      // Convert to lowercase
                .replaceAll("[^a-z0-9]", "-")  // Replace non-alphanumeric with "-"
                .replaceAll("-{2,}", "-")      // Replace multiple "-" with a single "-"
                .replaceAll("^-|-$", "");      // Remove leading and trailing "-"
    }


    /**
     * ✅ Converts Company Name to a Valid Schema Name.
     * Example: "XYZ Pvt Ltd" → "xyz_pvt_ltd"
     *
     * @param companyName The company name.
     * @return The schema-safe company name.
     */
    public static String convertCompanyNameToSchema(String companyName) {
        return companyName.trim()
                .replaceAll("[^a-zA-Z0-9]", "_")  // ✅ Replace special characters with `_`
                .replaceAll("_+", "_")           // ✅ Ensure no multiple consecutive `_`
                .toLowerCase();
    }

    /**
     * Converts a kebab/slug tenant id to Title Case words.
     * e.g. "nextgen-solutions-inc" -> "Nextgen Solutions Inc"
     *      "wfm-experts_india   pvt-ltd" -> "Wfm Experts India Pvt Ltd"
     *
     * @param tenantId the tenant id (kebab/slug)
     * @return Title Cased company name approximation
     */
    public static String tenantIdToCompanyName(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant id cannot be empty when converting to company name.");
        }

        // Normalize and split on -, _, or whitespace (handles multiple separators)
        String[] parts = tenantId
                .trim()
                .toLowerCase(Locale.ROOT)
                .split("[-_\\s]+");

        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            // Capitalize first letter, keep the rest as-is (already lowercased)
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1));
            }
            sb.append(' ');
        }
        return sb.toString().trim();
    }

}
