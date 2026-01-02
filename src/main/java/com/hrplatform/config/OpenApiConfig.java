package com.hrplatform.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HR Document Management Platform API",
                version = "1.0",
                description = "API documentation for HR Document Management Platform. " +
                        "This platform allows staff to submit documents and HR users to manage submissions.",
                contact = @Contact(
                        name = "HR Platform Support",
                        email = "support@hrplatform.com"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:9090",
                        description = "Development Server"
                ),
                @Server(
                        url = "https://api.hrplatform.com",
                        description = "Production Server"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER,
        description = "Enter JWT Bearer token"
)
public class OpenApiConfig {
}