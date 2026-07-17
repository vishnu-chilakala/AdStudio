package com.cts.creative.creativeconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI creativeOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Creative Service API")
                                .version("1.0")
                                .description("AdStudio Creative Microservice")
                                .contact(
                                        new Contact()
                                                .name("CTS Team")
                                                .email("support@adstudio.com")
                                )
                );
    }
}