package com.fintrack.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FinTrack API",
                version = "1.0.0",
                description = "Personal finance REST API — JWT-authenticated, PostgreSQL-backed, deployed on AWS.",
                contact = @Contact(name = "FinTrack Support")
        ),
        tags = {
                @Tag(name = "Authentication", description = "User registration and JWT login"),
                @Tag(name = "Transactions", description = "Core income and expense management"),
                @Tag(name = "Summary", description = "Financial reporting and category breakdowns"),
                @Tag(name = "Health", description = "System monitoring and uptime")
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter JWT Bearer token **_only_**"
)
public class OpenApiConfig {
}
