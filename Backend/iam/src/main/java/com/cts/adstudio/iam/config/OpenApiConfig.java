package com.cts.adstudio.iam.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Single Swagger / OpenAPI configuration for the whole monolith.
 *
 * <p>Replaces the four per-service OpenAPI beans that previously conflicted. It defines:</p>
 * <ul>
 *   <li>one global {@link OpenAPI} bean carrying the API info and an HTTP Bearer-JWT
 *       security scheme, so the Swagger UI "Authorize" button accepts the login token
 *       on every group; and</li>
 *   <li>one {@link GroupedOpenApi} per business module, which makes Swagger UI show a
 *       group dropdown (IAM, Advertiser, Media Plan, Finance, Creative, Notification, Delivery).</li>
 * </ul>
 *
 * <p>Swagger UI: {@code /swagger-ui.html} &middot; aggregate docs: {@code /v3/api-docs} &middot;
 * per-group docs: {@code /v3/api-docs/{group}}. Log in via {@code POST /api/auth/login}
 * (seeded admin: {@code admin@adstudio.com} / {@code Admin@123}) and paste the token.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI adStudioOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AdStudio API")
                        .description("Unified AdStudio backend: identity & access, advertiser & campaign, "
                                + "media planning, finance & billing, creative, notification, and delivery APIs.")
                        .version("1.0.0")
                        .contact(new Contact().name("AdStudio Team").email("support@adstudio.com"))
                        .license(new License().name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by POST /api/auth/login")));
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0-all")
                .displayName("All APIs")
                .packagesToScan("com.cts.adstudio")
                .build();
    }

    @Bean
    public GroupedOpenApi iamApi() {
        return GroupedOpenApi.builder()
                .group("1-iam")
                .displayName("Identity & Access")
                .packagesToScan("com.cts.adstudio.iam")
                .build();
    }

    @Bean
    public GroupedOpenApi advertiserApi() {
        return GroupedOpenApi.builder()
                .group("2-advertiser")
                .displayName("Advertiser & Campaign")
                .packagesToScan("com.cts.adstudio.advertiser")
                .build();
    }

    @Bean
    public GroupedOpenApi mediaplanApi() {
        return GroupedOpenApi.builder()
                .group("3-mediaplan")
                .displayName("Media Plan")
                .packagesToScan("com.cts.adstudio.mediaplan")
                .build();
    }

    @Bean
    public GroupedOpenApi financeApi() {
        return GroupedOpenApi.builder()
                .group("4-finance")
                .displayName("Finance & Billing")
                .packagesToScan("com.cts.adstudio.finance")
                .build();
    }

    @Bean
    public GroupedOpenApi creativeApi() {
        return GroupedOpenApi.builder()
                .group("5-creative")
                .displayName("Creative")
                .packagesToScan("com.cts.adstudio.creative")
                .build();
    }

    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("6-notification")
                .displayName("Notification")
                .packagesToScan("com.cts.adstudio.notification")
                .build();
    }

    @Bean
    public GroupedOpenApi deliveryApi() {
        return GroupedOpenApi.builder()
                .group("7-delivery")
                .displayName("Delivery & Performance")
                .packagesToScan("com.cts.adstudio.delivery")
                .build();
    }
}
