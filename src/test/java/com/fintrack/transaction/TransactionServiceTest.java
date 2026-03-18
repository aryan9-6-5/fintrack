package com.fintrack.transaction;

import com.fintrack.auth.AuthRepository;
import com.fintrack.auth.User;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.fraud.FraudDetectionService;
import com.fintrack.transaction.dto.CategorySummaryResponse;
import com.fintrack.transaction.dto.TransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private String testEmail = "test@example.com";
    private TransactionRequest request;
    private Transaction entity;
    private TransactionResponse responseDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(testEmail);

        request = new TransactionRequest();
        request.setAmount(new BigDecimal("100"));
        request.setType(TransactionType.EXPENSE);
        request.setCategory("Food");

        entity = new Transaction();
        entity.setId(100L);
        entity.setUser(testUser);
        entity.setAmount(new BigDecimal("100"));
        entity.setType(TransactionType.EXPENSE);
        entity.setFlagged(false);

        responseDto = new TransactionResponse();
        responseDto.setId(100L);
        responseDto.setAmount(new BigDecimal("100"));
    }

    @Test
    @DisplayName("Create transaction successfully")
    void createTransaction() {
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(transactionMapper.toEntity(request)).thenReturn(entity);
        when(transactionRepository.getAverageAmountByUserId(testUser.getId())).thenReturn(BigDecimal.ZERO);
        when(fraudDetectionService.isFraudulent(request.getAmount(), BigDecimal.ZERO)).thenReturn(false);
        when(transactionRepository.save(entity)).thenReturn(entity);
        when(transactionMapper.toResponse(entity)).thenReturn(responseDto);

        TransactionResponse result = transactionService.create(testEmail, request);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(entity);
    }

    @Test
    @DisplayName("Throw ResourceNotFoundException if user accesses another's transaction")
    void getByIdWrongUser() {
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getById(100L, testEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transaction not found");
    }

    @Test
    @DisplayName("Get all transactions for user")
    void getAllForUser() {
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findAllByUserId(1L)).thenReturn(List.of(entity));
        when(transactionMapper.toResponse(entity)).thenReturn(responseDto);

        List<TransactionResponse> result = transactionService.getAll(testEmail);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Delete transaction successfully")
    void deleteTransaction() {
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(entity));

        transactionService.delete(100L, testEmail);

        verify(transactionRepository).delete(entity);
    }
    
    @Test
    @DisplayName("Update transaction successfully - triggering fraud flag")
    void updateTransaction() {
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(entity));
        
        request.setAmount(new BigDecimal("3000"));
        when(transactionRepository.getAverageAmountByUserId(1L)).thenReturn(new BigDecimal("100"));
        when(fraudDetectionService.isFraudulent(new BigDecimal("3000"), new BigDecimal("100"))).thenReturn(true);
        when(transactionRepository.save(entity)).thenReturn(entity);
        when(transactionMapper.toResponse(entity)).thenReturn(responseDto);

        TransactionResponse result = transactionService.update(100L, testEmail, request);

        assertThat(result).isNotNull();
        verify(transactionMapper).updateEntityFromRequest(request, entity);
        assertThat(entity.isFlagged()).isTrue();
        verify(transactionRepository).save(entity);
    }

    @Test
    @DisplayName("Get summary returns projected summaries")
    void getSummary() {
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        CategorySummaryResponse summary = new CategorySummaryResponse("Food", new BigDecimal("100"));
        when(transactionRepository.getSummary(1L, null, null)).thenReturn(List.of(summary));

        List<CategorySummaryResponse> results = transactionService.getSummary(testEmail, null, null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCategory()).isEqualTo("Food");
    }
}
