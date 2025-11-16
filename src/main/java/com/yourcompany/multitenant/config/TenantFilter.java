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

    // SUPER ADMIN tenant ID always "1"
    public static final String SUPER_ADMIN_ID = "1";

    // Your root domain (example: sarthak.cfd)
    @Value("${app.base.domain:localhost}")
    private String baseDomain;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        // 🔥 Fix for Render / Reverse Proxy
        String serverName = req.getHeader("X-Forwarded-Host");
        if (serverName == null || serverName.isEmpty()) {
            serverName = req.getServerName();
        }

        log.debug("TenantFilter host detected: {}", serverName);

        String tenantId = resolveTenantId(serverName);

        if (tenantId != null) {
            log.debug("TenantContext set → {}", tenantId);
            TenantContext.setTenantId(tenantId);
        } else {
            log.warn("Tenant could NOT be resolved from domain: {}", serverName);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    // 🔍 MAIN TENANT RESOLVER
    private String resolveTenantId(String serverName) {
        if (serverName == null) return null;

        // Localhost → superadmin
        if (serverName.equals("127.0.0.1") || serverName.equalsIgnoreCase("localhost")) {
            return SUPER_ADMIN_ID;
        }

        // Root/base domain → superadmin
        if (isBaseDomain(serverName)) {
            return SUPER_ADMIN_ID;
        }

        // Subdomain → tenant
        String subdomain = extractSubdomain(serverName);
        if (subdomain != null && !subdomain.isEmpty()) {
            return subdomain;
        }

        return null;
    }

    // 🟢 CHECK BASE DOMAIN (sarthak.cfd)
    private boolean isBaseDomain(String serverName) {

        // Handle www.sarthak.cfd as base domain too
        if (serverName.equalsIgnoreCase("www." + baseDomain)) {
            return true;
        }

        return serverName.equalsIgnoreCase(baseDomain);
    }

    // 🟢 EXTRACT TENANT SUBDOMAIN: him.sarthak.cfd → "him"
    private String extractSubdomain(String serverName) {

        // Ignore IP addresses
        if (serverName.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
            return null;
        }

        String[] parts = serverName.split("\\.");
        String[] baseParts = baseDomain.split("\\.");

        // Subdomain ONLY if parts = baseParts + 1
        // Example: him.sarthak.cfd → 3 parts, baseDomain = 2 parts
        if (parts.length != baseParts.length + 1) {
            return null; // prevents sarthak.cfd from becoming tenant=sarthak
        }

        // Check end matches baseDomain
        for (int i = 0; i < baseParts.length; i++) {
            if (!parts[parts.length - baseParts.length + i].equalsIgnoreCase(baseParts[i])) {
                return null;
            }
        }

        // Return first segment → tenant
        return parts[0];
    }
}
