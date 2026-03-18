package com.fintrack.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.auth.AuthRepository;
import com.fintrack.auth.User;
import com.fintrack.common.security.JwtUtil;
import com.fintrack.transaction.dto.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String validJwtUserA;
    private String validJwtUserB;
    private Transaction userATransaction;

    @BeforeEach
    void setUp() {
        User userA = new User();
        userA.setEmail("userA@example.com");
        userA.setPassword("password");
        authRepository.save(userA);

        User userB = new User();
        userB.setEmail("userB@example.com");
        userB.setPassword("password");
        authRepository.save(userB);

        validJwtUserA = jwtUtil.generateToken(userA.getEmail());
        validJwtUserB = jwtUtil.generateToken(userB.getEmail());

        userATransaction = new Transaction();
        userATransaction.setUser(userA);
        userATransaction.setAmount(new BigDecimal("50.00"));
        userATransaction.setType(TransactionType.EXPENSE);
        userATransaction.setCategory("Groceries");
        userATransaction.setFlagged(false);
        transactionRepository.save(userATransaction);
    }

    @Test
    @DisplayName("Create transaction with valid JWT")
    void createWithValidJwt() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.INCOME);
        request.setCategory("Salary");

        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + validJwtUserA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    @DisplayName("Reject request with invalid JWT")
    void rejectInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get all transactions for authenticated user")
    void getAllForUser() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer " + validJwtUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(50.00));
    }

    @Test
    @DisplayName("Return 404/403 when accessing another user's transaction")
    void crossUserAccessBlocked() throws Exception {
        mockMvc.perform(get("/api/transactions/" + userATransaction.getId())
                .header("Authorization", "Bearer " + validJwtUserB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update transaction with valid JWT")
    void updateWithValidJwt() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("75.00"));
        request.setType(TransactionType.EXPENSE);
        request.setCategory("Groceries");

        mockMvc.perform(put("/api/transactions/" + userATransaction.getId())
                .header("Authorization", "Bearer " + validJwtUserA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(75.00));
    }

    @Test
    @DisplayName("Delete transaction with valid JWT")
    void deleteWithValidJwt() throws Exception {
        mockMvc.perform(delete("/api/transactions/" + userATransaction.getId())
                .header("Authorization", "Bearer " + validJwtUserA))
                .andExpect(status().isNoContent());
    }
}
