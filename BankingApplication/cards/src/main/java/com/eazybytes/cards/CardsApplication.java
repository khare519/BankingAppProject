package com.eazybytes.cards;

import com.eazybytes.cards.dto.CardsContactInfoDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing(auditorAwareRef="auditAwareImpl")
@EnableConfigurationProperties(value =  CardsContactInfoDto.class)
@OpenAPIDefinition(
		info=@Info(
				title = "Cards Microservices REST API Documentation",
				version = "v1",
				description = "Cards Microservices REST API Documentation",
				contact = @Contact(
						name = "Satyam Khare",
						email ="satyam123@gmail.com",
						url="https://www.google.com"
				),
				license = @License(
						name="Apache 2.0",
						url="https://www.google.com"
				)
		),
		externalDocs =@ExternalDocumentation(
           description = "For Any other information",
				url = "https://www.google.com"
		)
)
public class CardsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardsApplication.class, args);
	}

}
