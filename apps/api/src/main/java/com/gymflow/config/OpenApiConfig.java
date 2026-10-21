package com.gymflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadata for the runtime OpenAPI document served at {@code /v3/api-docs}.
 * The reviewed contract lives in {@code contracts/openapi.yaml}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI gymFlowOpenApi() {
        return new OpenAPI().info(new Info()
            .title("GymFlow API")
            .version("v1")
            .description("Staff dashboard and public renewal API for GymFlow."));
    }
}
