package com.creditbook.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.creditbook.dto.LoginRequest;
import com.creditbook.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Authentication Controller Integration Tests")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Successfully register a new merchant user")
    void testRegisterNewUserSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Sunil Narine",
                "sunil.test." + System.currentTimeMillis() + "@creditbook.com",
                "Secret@123",
                "Narine Provisions",
                "+91 9123456780"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.fullName").value("Sunil Narine"));
    }

    @Test
    @DisplayName("Successfully log in with seeded demo credentials")
    void testLoginDemoUserSuccess() throws Exception {
        LoginRequest request = new LoginRequest("demo@creditbook.com", "Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("demo@creditbook.com"))
                .andExpect(jsonPath("$.fullName").value("Rajesh Sharma"));
    }

    @Test
    @DisplayName("Reject login with incorrect password")
    void testLoginInvalidPasswordFails() throws Exception {
        LoginRequest request = new LoginRequest("demo@creditbook.com", "WrongPassword999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Reject unauthenticated access to protected customer endpoint")
    void testUnauthenticatedAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }
}
