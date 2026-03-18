package com.fintrack.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "7yR9sT2vW5xZ8aB1cD4eF6gH9iJ2kL5mN8oP1qR4sT7uV0wX3yZ6aB9cD2eF5gH8iJ");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3600000L);

        userDetails = User.withUsername("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("Should generate token and extract username correctly")
    void shouldGenerateAndExtractToken() {
        String token = jwtUtil.generateToken(userDetails.getUsername());

        assertThat(token).isNotBlank();
        
        String extractedUsername = jwtUtil.extractUsername(token);
        assertThat(extractedUsername).isEqualTo(userDetails.getUsername());
    }

    @Test
    @DisplayName("Should validate correct token")
    void shouldValidateCorrectToken() {
        String token = jwtUtil.generateToken(userDetails.getUsername());
        
        boolean isValid = jwtUtil.isTokenValid(token, userDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject token for different user")
    void shouldRejectTokenForDifferentUser() {
        String token = jwtUtil.generateToken("other@example.com");
        
        boolean isValid = jwtUtil.isTokenValid(token, userDetails);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should throw exception on malformed token")
    void shouldThrowOnMalformed() {
        String tamperedToken = "header.payload.invalidSignature";
        
        assertThatThrownBy(() -> jwtUtil.extractUsername(tamperedToken))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}
