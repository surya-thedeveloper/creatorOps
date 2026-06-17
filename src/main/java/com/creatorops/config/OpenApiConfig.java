package com.creatorops.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * <h3>Why this class exists</h3>
 * Configures the OpenAPI 3.0 specification for CreatorOps. Registers a global
 * Bearer JWT security scheme so all authenticated endpoints show the Authorize
 * button in Swagger UI without requiring per-endpoint annotation.
 * <p>
 * <h3>Access</h3>
 * <ul>
 *   <li>Swagger UI: {@code /swagger-ui/index.html}</li>
 *   <li>OpenAPI JSON: {@code /v3/api-docs}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI creatorOpsOpenApi() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.creatorops.io").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, buildBearerSecurityScheme())
                );
    }

    private Info buildApiInfo() {
        return new Info()
                .title("CreatorOps API")
                .description("""
                        Content Operations Platform API — manages the full lifecycle from idea to publication.
                        
                        **Authentication**: All endpoints (except /api/v1/auth/*) require a valid JWT Bearer token.
                        Obtain a token via `POST /api/v1/auth/login`, then click **Authorize** and enter:
                        `Bearer <your-token>`
                        
                        **Pagination**: List endpoints accept `page`, `size`, and `sort` query parameters.
                        
                        **Error Responses**: All errors follow RFC 7807 Problem Details format.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("CreatorOps Team")
                        .email("dev@creatorops.io"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://creatorops.io"));
    }

    private SecurityScheme buildBearerSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT token. Obtain it from POST /api/v1/auth/login");
    }
}
