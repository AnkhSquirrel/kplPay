package com.ankhsquirrel.kplpay.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kplPayOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("kplPay API")
                .description("Simulated B2B SaaS subscription billing backend: customer "
                        + "onboarding via INSEE Sirene lookup, subscription management, and "
                        + "invoice generation with VAT calculation.")
                .version("v1"));
    }
}
