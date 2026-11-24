package com.anmol.trusteval.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trustEvalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TrustEval — Anti-Spam Task Allocation & Evaluator Trust Engine API")
                        .version("1.0.0")
                        .description("REST API utilizing Max-Heap Priority Queue routing, Honey-Pot spam detection, and Weighted Consensus resolution.")
                        .contact(new Contact()
                                .name("Anmol Chandel")
                                .email("anmolchandel13@gmail.com")));
    }
}
