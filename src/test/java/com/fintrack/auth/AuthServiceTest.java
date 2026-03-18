package com.fintrack.auth;

import com.fintrack.auth.dto.AuthResponse;
import com.fintrack.auth.dto.LoginRequest;
import com.fintrack.auth.dto.RegisterRequest;
import com.fintrack.common.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthRepository authRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should successfully register a new user")
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password");
        
        when(authRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_password");
        when(jwtUtil.generateToken(request.getEmail())).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("mocked_jwt_token");
        verify(authRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when registering duplicate email")
    void registerDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password");
        
        when(authRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already registered");
                
        verify(authRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully")
    void loginSuccess() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        User mockUser = new User();
        mockUser.setEmail(request.getEmail());
        
        when(authRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(request.getEmail())).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("mocked_jwt_token");
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
    }

    @Test
    @DisplayName("Should throw exception on wrong password")
    void loginWrongPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong_password");
        
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Should throw exception when login user is not found")
    void loginUserNotFound() {
        LoginRequest request = new LoginRequest("notfound@example.com", "password");
        
        when(authRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }
}
