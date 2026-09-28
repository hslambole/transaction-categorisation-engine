package com.assessment.transaction_categorisation_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class TransactionCategorisationEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionCategorisationEngineApplication.class, args);
	}

}
