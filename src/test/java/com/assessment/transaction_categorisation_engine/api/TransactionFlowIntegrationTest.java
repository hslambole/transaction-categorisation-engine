package com.assessment.transaction_categorisation_engine.api;

import com.assessment.transaction_categorisation_engine.domain.Source;
import com.assessment.transaction_categorisation_engine.repository.MerchantCategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class TransactionFlowIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("transactions")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MerchantCategoryRepository merchantCategoryRepository;

    @Test
    void bulkTriggerAndListFlow() throws Exception {
        String payload = """
                [
                  {
                    "merchantName": "STARBUCKS PUNE FC",
                    "amount": 420.50,
                    "currency": "INR",
                    "transactionDate": "%s",
                    "source": "%s"
                  },
                  {
                    "merchantName": "RANDOM UNKNOWN SHOP",
                    "amount": 99.00,
                    "currency": "INR",
                    "transactionDate": "%s",
                    "source": "MANUAL"
                  }
                ]
                """.formatted(LocalDate.now(), Source.BANK_FEED, LocalDate.now());

        mockMvc.perform(post("/api/v1/transactions/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(2))
                .andExpect(jsonPath("$.rejected").value(0));

        mockMvc.perform(post("/api/v1/categorise/trigger"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("TRIGGERED"));

        await().untilAsserted(() ->
                assertThat(merchantCategoryRepository.findByMerchantName("STARBUCKS PUNE FC")).isPresent()
        );

        MvcResult listed = mockMvc.perform(get("/api/v1/transactions")
                        .param("category", "Food")
                        .param("source", "BANK_FEED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andReturn();

        JsonNode body = objectMapper.readTree(listed.getResponse().getContentAsString());
        assertThat(body.get("content").get(0).get("merchantName").asText()).isEqualTo("STARBUCKS PUNE FC");
        assertThat(body.get("content").get(0).get("category").asText()).isEqualTo("Food");
        assertThat(body.get("content").get(0).get("subCategory").asText()).isEqualTo("Coffee");

        mockMvc.perform(get("/api/v1/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topMerchants").isArray())
                .andExpect(jsonPath("$.spendByCategory").isArray())
                .andExpect(jsonPath("$.monthOverMonth.currentMonthSpend").exists());
    }
}
