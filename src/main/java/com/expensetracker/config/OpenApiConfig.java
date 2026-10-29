package com.expensetracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI expenseTrackerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Expense Tracker API")
                .description("REST API to manage users, expense categories and expenses, "
                        + "with filtering, pagination and a monthly spending report.")
                .version("v1")
                .license(new License().name("MIT License")));
    }
}
