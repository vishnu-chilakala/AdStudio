package com.cts.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Billing & Reconciliation microservice. Standalone Spring Boot service with its
 * own database; references advertisers / briefs / publishers / insertion orders
 * by id (those entities live in other services).
 */

@SpringBootApplication
public class FinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceApplication.class, args);
	}

}
