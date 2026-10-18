package com.linguanotes.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String SESSION_COOKIE = "session";

    @Bean
    OpenAPI linguanotesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("LinguaNotes API")
                        .version("1.0.0")
                        .description("API REST de gestion de notes et d'apprentissage des langues"))
                .components(new Components().addSecuritySchemes(SESSION_COOKIE,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")))
                .addSecurityItem(new SecurityRequirement().addList(SESSION_COOKIE));
    }
}
