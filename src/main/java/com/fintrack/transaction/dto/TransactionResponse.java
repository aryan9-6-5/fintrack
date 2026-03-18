package com.fintrack.transaction.dto;

import com.fintrack.transaction.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private String description;
    private boolean isFlagged;
    private LocalDateTime createdAt;
}
