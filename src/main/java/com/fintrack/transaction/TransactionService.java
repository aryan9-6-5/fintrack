package com.fintrack.transaction;

import com.fintrack.auth.AuthRepository;
import com.fintrack.auth.User;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.fraud.FraudDetectionService;
import com.fintrack.transaction.dto.CategorySummaryResponse;
import com.fintrack.transaction.dto.TransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuthRepository authRepository;
    private final TransactionMapper transactionMapper;
    private final FraudDetectionService fraudDetectionService;

    private User getUserByEmail(String email) {
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public TransactionResponse create(String userEmail, TransactionRequest request) {
        User user = getUserByEmail(userEmail);

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setUser(user);

        BigDecimal average = transactionRepository.getAverageAmountByUserId(user.getId());
        boolean isFraud = fraudDetectionService.isFraudulent(request.getAmount(), average);
        transaction.setFlagged(isFraud);

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAll(String userEmail) {
        User user = getUserByEmail(userEmail);
        return transactionRepository.findAllByUserId(user.getId()).stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse update(Long id, String userEmail, TransactionRequest request) {
        User user = getUserByEmail(userEmail);
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        transactionMapper.updateEntityFromRequest(request, transaction);

        BigDecimal average = transactionRepository.getAverageAmountByUserId(user.getId());
        boolean isFraud = fraudDetectionService.isFraudulent(request.getAmount(), average);
        transaction.setFlagged(isFraud);

        Transaction updated = transactionRepository.save(transaction);
        return transactionMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getSummary(String userEmail, Integer month, Integer year) {
        User user = getUserByEmail(userEmail);
        return transactionRepository.getSummary(user.getId(), month, year);
    }
}
