package com.example.loginapp.controller;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


import com.example.loginapp.model.SSOConfig;
import com.example.loginapp.repository.SSOConfigRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Base64;

@Controller
@RequestMapping("/sso/saml")
public class SAMLController {

    @Autowired
    private SSOConfigRepository ssoConfigRepository;

    /**
     * Redirects the user to miniOrange IdP login page.
     */
    @GetMapping("/login")
    public String samlLogin(HttpServletRequest request) {
        try {
            SSOConfig config = ssoConfigRepository.findTopByOrderByIdDesc();
            if (config == null || !config.isSamlEnabled() || config.getSamlUrl() == null) {
                return "redirect:/error?message=SAML not configured";
            }

            // Base URL (your app)
            String baseUrl = request.getRequestURL().toString()
                    .replace(request.getRequestURI(), request.getContextPath());

            String acsUrl = baseUrl + "/sso/saml/callback";
            String issuer = baseUrl + "/sso/saml/metadata";

            // 🧩 Build AuthnRequest XML
            String authnRequest = """
                <samlp:AuthnRequest xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol"
                    ID="_12345"
                    Version="2.0"
                    IssueInstant="2025-10-31T12:00:00Z"
                    ProtocolBinding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
                    AssertionConsumerServiceURL="%s">
                    <saml:Issuer xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion">%s</saml:Issuer>
                    <samlp:NameIDPolicy AllowCreate="true"
                        Format="urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress"/>
                </samlp:AuthnRequest>
                """.formatted(acsUrl, issuer);

            // 🗜️ Deflate + Base64 encode
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Deflater deflater = new Deflater(Deflater.DEFLATED, true);
            DeflaterOutputStream deflaterStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
            deflaterStream.write(authnRequest.getBytes(StandardCharsets.UTF_8));
            deflaterStream.close();

            String samlRequest = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
            String encodedRequest = URLEncoder.encode(samlRequest, StandardCharsets.UTF_8);

            // 🔗 Redirect to IdP with SAMLRequest param
            String redirectUrl = config.getSamlUrl() + "?SAMLRequest=" + encodedRequest;

            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error?message=" + e.getMessage();
        }
    }


    /**
     * Handles the SAML Response POSTed by the IdP after successful login.
     */
    @PostMapping("/callback")
    public String samlCallback(@RequestParam(value = "SAMLResponse", required = false) String samlResponse,
                               Model model) {
        try {
            if (samlResponse == null || samlResponse.isEmpty()) {
                model.addAttribute("error", "No SAML Response received.");
                return "error";
            }

            // Decode Base64 SAML Response
            byte[] decodedBytes = Base64.getDecoder().decode(samlResponse);
            String xml = new String(decodedBytes);

            // Parse XML
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(xml.getBytes()));
            document.getDocumentElement().normalize();

            // Extract user info (NameID / Email)
            String nameId = document.getElementsByTagName("saml:NameID").item(0).getTextContent();

            if (nameId == null || nameId.isEmpty()) {
                model.addAttribute("error", "Invalid SAML response: NameID not found.");
                return "error";
            }

            // ✅ Successful login
            model.addAttribute("userEmail", nameId);
            model.addAttribute("message", "SAML Login Successful!");
            return "home"; // Or redirect to dashboard.html

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error processing SAML Response: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Test SSO Configuration button (redirects to IdP)
     */
    @GetMapping("/test")
    public String testSamlConfig(Model model) {
        SSOConfig config = ssoConfigRepository.findTopByOrderByIdDesc();
        if (config == null || !config.isSamlEnabled() || config.getSamlUrl() == null) {
            model.addAttribute("error", "SAML is not enabled in your configuration.");
            return "error";
        }
        return "redirect:" + config.getSamlUrl();
    }

    /**
     * Bonus: Metadata endpoint for IdP to configure SP automatically.
     */
    @GetMapping(value = "/metadata", produces = "application/xml")
    @ResponseBody
    public String metadata(HttpServletRequest request) {
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());

        // Entity ID (SP Issuer)
        String entityId = baseUrl + "/sso/saml/metadata";

        // Assertion Consumer Service (ACS) URL
        String acsUrl = baseUrl + "/sso/saml/callback";

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<EntityDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\" entityID=\"" + entityId + "\">"
                + "<SPSSODescriptor WantAssertionsSigned=\"false\" AuthnRequestsSigned=\"false\" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">"
                + "<AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"" + acsUrl + "\" index=\"1\"/>"
                + "</SPSSODescriptor>"
                + "</EntityDescriptor>";
    }
}