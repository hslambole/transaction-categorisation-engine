package com.assessment.transaction_categorisation_engine;

import org.springframework.boot.SpringApplication;

public class TestTransactionCategorisationEngineApplication {

	public static void main(String[] args) {
		SpringApplication.from(TransactionCategorisationEngineApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
