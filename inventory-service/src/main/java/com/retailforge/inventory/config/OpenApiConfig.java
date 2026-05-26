package com.retailforge.inventory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String oauthSchemeName = "Keycloak_OAuth2";
        final String bearerSchemeName = "Bearer_Token";

        return new OpenAPI()
            .servers(List.of(new io.swagger.v3.oas.models.servers.Server().url("/")))
            .info(new Info()
                .title("RetailForge Inventory Service API")
                .version("1.0.0")
                .description("Microservice REST API documentation for managing stock levels and warehouse inventory in RetailForge."))
            .addSecurityItem(new SecurityRequirement()
                .addList(oauthSchemeName)
                .addList(bearerSchemeName))
            .components(new Components()
                .addSecuritySchemes(oauthSchemeName, new SecurityScheme()
                    .name(oauthSchemeName)
                    .type(SecurityScheme.Type.OAUTH2)
                    .description("Authenticate using Keycloak OpenID Connect")
                    .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                            .authorizationUrl("http://localhost:8080/realms/retailforge/protocol/openid-connect/auth")
                            .tokenUrl("http://localhost:8080/realms/retailforge/protocol/openid-connect/token")
                            .scopes(new Scopes()))))
                .addSecuritySchemes(bearerSchemeName, new SecurityScheme()
                    .name(bearerSchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Or manually supply a Bearer JWT Token directly")));
      }
}
