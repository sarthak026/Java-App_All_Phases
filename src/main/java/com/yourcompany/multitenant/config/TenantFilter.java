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

    // Base domain (example: sarthak.cfd)
    @Value("${app.base.domain:localhost}")
    private String baseDomain;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        // Handle proxies (Render, Cloudflare)
        String serverName = req.getHeader("X-Forwarded-Host");
        if (serverName == null || serverName.isEmpty()) {
            serverName = req.getServerName();
        }

        log.debug("TenantFilter - Detected host: {}", serverName);

        String tenantId = resolveTenantId(serverName);

        if (tenantId != null) {
            log.debug("Tenant resolved → {}", tenantId);
            TenantContext.setTenantId(tenantId);
        } else {
            log.warn("Tenant could NOT be resolved for domain: {}", serverName);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    // MAIN tenant resolver
    private String resolveTenantId(String serverName) {
        if (serverName == null) return null;

        // Local environment
        if (serverName.equals("127.0.0.1") || serverName.equalsIgnoreCase("localhost")) {
            return SUPER_ADMIN_ID;
        }

        // EXACT DOMAIN or www.DOMAIN = SUPERADMIN
        if (isBaseDomain(serverName)) {
            return SUPER_ADMIN_ID;
        }

        // SUBDOMAIN.ROOTDOMAIN → TENANT
        String subdomain = extractSubdomain(serverName);
        if (subdomain != null && !subdomain.isBlank()) {
            return subdomain;
        }

        return null;
    }

    // Check root domain
    private boolean isBaseDomain(String serverName) {
        return serverName.equalsIgnoreCase(baseDomain)
                || serverName.equalsIgnoreCase("www." + baseDomain);
    }

    // Extract tenant → him.sarthak.cfd → him
    private String extractSubdomain(String serverName) {

        // If IP → skip
        if (serverName.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
            return null;
        }

        String[] parts = serverName.split("\\.");
        String[] baseParts = baseDomain.split("\\.");

        // Must be exactly 1 part MORE than base domain → prevents mistake
        if (parts.length != baseParts.length + 1) {
            return null;
        }

        // Validate domain end matches baseDomain
        for (int i = 0; i < baseParts.length; i++) {
            if (!parts[parts.length - baseParts.length + i]
                    .equalsIgnoreCase(baseParts[i])) {
                return null;
            }
        }

        // Return first part = tenant id
        return parts[0];
    }
}
