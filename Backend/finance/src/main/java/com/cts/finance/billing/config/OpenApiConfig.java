package com.cts.finance.billing.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI definition for the Billing service.
 *
 * Swagger UI: /swagger-ui.html   ·   OpenAPI JSON: /api/docs
 *
 * Declares an HTTP bearer (JWT) scheme so the "Authorize" button in Swagger UI
 * lets you paste a token from the IAM service and call the secured endpoints.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI billingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AdStudio Billing Service API")
                        .version("0.1.0")
                        .description("Billing & Reconciliation microservice: client invoices, "
                                + "publisher invoice reconciliation, payment tracking, and the "
                                + "billing calendar."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
