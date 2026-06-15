package org.example.storemanager.security;

import org.springframework.stereotype.Component;

@Component("securityEvaluator")
public class SecurityEvaluator {

    /**
     * Placeholder method for dynamic authorization checking.
     * You can easily extend this method to check current user permissions
     * stored in Database, Redis, or Spring Security Authorities.
     *
     * @param permission the permission key, e.g. "catalog:product:view"
     * @return true if permitted, false otherwise
     */
    public boolean hasPermission(String permission) {
        // Placeholder implementation: return true for development mode.
        // Replace this with actual database/role checking logic later.
        return true;
    }
}
