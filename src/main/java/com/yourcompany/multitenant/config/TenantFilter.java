package com.yourcompany.multitenant.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class TenantFilter implements Filter {

    // SUPER ADMIN tenant ID
    public static final String SUPER_ADMIN_ID = "1";

    // Base domain: example -> sarthak.cfd (configured in application.yml)
    @Value("${app.base.domain:localhost}")
    private String baseDomain;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String host = req.getHeader("X-Forwarded-Host");
        if (host == null || host.isEmpty()) {
            host = req.getServerName();
        }

        log.debug("TenantFilter - Host detected: {}", host);

        String tenantId = resolveTenantId(host);

        if (tenantId != null) {
            log.debug("Tenant resolved = {}", tenantId);
            TenantContext.setTenantId(tenantId);
        } else {
            log.warn("Tenant could NOT be resolved for host: {}", host);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Resolve tenant ID based on host rules:
     * - base domain -> SUPERADMIN
     * - subdomain.baseDomain -> subdomain
     */
    private String resolveTenantId(String host) {

        if (host == null) return null;

        host = host.toLowerCase();

        // Local dev -> SUPERADMIN
        if (host.equals("localhost") || host.equals("127.0.0.1")) {
            return SUPER_ADMIN_ID;
        }

        // Direct match to base domain -> SUPERADMIN
        if (isBaseDomain(host)) {
            return SUPER_ADMIN_ID;
        }

        // Resolve subdomain tenant
        String subdomain = resolveSubdomainTenant(host);
        if (subdomain != null) {
            return subdomain;
        }

        return SUPER_ADMIN_ID; // fallback if needed
    }

    private boolean isBaseDomain(String host) {
        return host.equals(baseDomain)
                || host.equals("www." + baseDomain);
    }

    /**
     * Extract tenant → sarthak.sarthak.cfd → sarthak
     * Only when host has exactly 1 extra part:
     *   parts = 3, base = 2 → OK
     */
    private String resolveSubdomainTenant(String host) {

        if (host.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
            return null;  // skip IPs
        }

        String[] hostParts = host.split("\\.");
        String[] baseParts = baseDomain.split("\\.");

        // subdomain.baseDomain must have exactly 1 more part
        if (hostParts.length != baseParts.length + 1) {
            return null;
        }

        // Ensure last parts match base domain
        for (int i = 0; i < baseParts.length; i++) {
            if (!hostParts[hostParts.length - baseParts.length + i].equals(baseParts[i])) {
                return null;
            }
        }

        // First part = tenant ID
        return hostParts[0];
    }
}
