package com.creditbook.controller;

import com.creditbook.dto.CustomerRequest;
import com.creditbook.dto.LoginRequest;
import com.creditbook.dto.TransactionRequest;
import com.creditbook.entity.CustomerStatus;
import com.creditbook.entity.PaymentMethod;
import com.creditbook.entity.TransactionType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Customer and Transaction Full End-to-End API Integration Tests")
public class CustomerAndTransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void obtainAuthToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("demo@creditbook.com", "Password@123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        jwtToken = jsonNode.get("token").asText();
        assertNotNull(jwtToken);
    }

    @Test
    @DisplayName("Complete workflow: Create customer -> Give credit -> View ledger -> Record repayment -> Reverse")
    void testCompleteCustomerLedgerWorkflow() throws Exception {
        // 1. Create a new Customer
        CustomerRequest customerRequest = new CustomerRequest(
                "Subhash Rao",
                "98000" + (System.currentTimeMillis() % 100000),
                "subhash@example.com",
                "Bangalore Road",
                "Test ledger customer",
                CustomerStatus.ACTIVE,
                BigDecimal.ZERO
        );

        MvcResult createCustResult = mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Subhash Rao"))
                .andReturn();

        JsonNode custJson = objectMapper.readTree(createCustResult.getResponse().getContentAsString());
        long customerId = custJson.get("id").asLong();

        // 2. Record Credit Given (₹5,000.00)
        TransactionRequest creditTx = new TransactionRequest(
                customerId,
                TransactionType.CREDIT_GIVEN,
                new BigDecimal("5000.00"),
                null,
                "Lent for construction materials",
                PaymentMethod.CASH,
                null
        );

        MvcResult creditResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creditTx)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.transactionType").value("CREDIT_GIVEN"))
                .andReturn();

        JsonNode creditJson = objectMapper.readTree(creditResult.getResponse().getContentAsString());
        long creditTxId = creditJson.get("id").asLong();

        // 3. Verify Customer Ledger reflects ₹5,000 running balance
        mockMvc.perform(get("/api/customers/" + customerId + "/ledger")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCredit").value(5000.00))
                .andExpect(jsonPath("$.currentOutstandingBalance").value(5000.00))
                .andExpect(jsonPath("$.entries[0].runningBalance").value(5000.00));

        // 4. Record Partial Payment Received (₹1,500.00)
        TransactionRequest paymentTx = new TransactionRequest(
                customerId,
                TransactionType.PAYMENT_RECEIVED,
                new BigDecimal("1500.00"),
                null,
                "Partial repayment via UPI",
                PaymentMethod.UPI,
                "UPI-88129"
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentTx)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        // 5. Verify Customer Ledger reflects updated ₹3,500 running balance
        mockMvc.perform(get("/api/customers/" + customerId + "/ledger")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCredit").value(5000.00))
                .andExpect(jsonPath("$.totalPaid").value(1500.00))
                .andExpect(jsonPath("$.currentOutstandingBalance").value(3500.00));

        // 6. Verify Dashboard Summary updates live
        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").isNumber())
                .andExpect(jsonPath("$.totalOutstandingCredit").isNumber());

        // 7. Verify safe transaction reversal
        mockMvc.perform(post("/api/transactions/" + creditTxId + "/reverse")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Test reversal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reversed").value(true))
                .andExpect(jsonPath("$.paymentStatus").value("REVERSED"));
    }

    @Test
    @DisplayName("Verify deletion protection prevents deleting customer with active ledger history")
    void testCustomerDeletionProtection() throws Exception {
        // Customer ID 1 (Ramesh Patel) has active transactions seeded by DataInitializer
        mockMvc.perform(delete("/api/customers/1")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("active transaction record")));
    }
}
