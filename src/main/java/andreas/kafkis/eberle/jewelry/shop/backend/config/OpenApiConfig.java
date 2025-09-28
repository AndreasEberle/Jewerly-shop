package andreas.kafkis.eberle.jewelry.shop.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jewelryShopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jewelry Shop API")
                        .description("""
                                A comprehensive REST API for a jewelry e-commerce platform built with Spring Boot.
                                
                                ## Features
                                - 🔐 **Authentication**: JWT + Google OAuth2
                                - 🛍️ **Product Management**: CRUD operations with image support
                                - 📦 **Order Processing**: Complete order lifecycle management
                                - 💳 **Payment Integration**: Secure payment processing
                                - 📧 **Email Notifications**: Order confirmations and updates
                                - ☁️ **Cloud Storage**: AWS S3 integration for images
                                - 📊 **Analytics**: Business metrics and reporting
                                - 🔒 **Security**: Role-based access control
                                
                                ## Authentication
                                Most endpoints require authentication. Use the `/api/auth/login` endpoint to get a JWT token,
                                or use Google OAuth2 via `/oauth2/authorization/google`.
                                
                                Include the token in the Authorization header: `Bearer <your-token>`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Andreas Kafkis-Eberle")
                                .email("andreas.kafkis.eberle@gmail.com")
                                .url("https://github.com/your-username"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.jewelryshop.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /api/auth/login or OAuth2")));
    }
}

