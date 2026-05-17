package com.platform.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(
            @Value("${spring.application.name:AI Language Coach}") String serviceName,
            @Value("${springdoc.version:1.0.0}") String appVersion) {

        return new OpenAPI()
                .info(new Info()
                        .title(serviceName)
                        .version(appVersion)
                        .description("AI Language Coach - REST API documentation")
                        .contact(new Contact()
                                .name("AI Language Coach Team")
                                .email("support@language-coach.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://language-coach.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from POST /auth/login or POST /auth/register")));
    }
}