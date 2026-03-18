package com.fintrack.transaction.dto;

import com.fintrack.transaction.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Transaction details response")
public class TransactionResponse {
    @Schema(description = "Unique ID", example = "101")
    private Long id;
    @Schema(description = "Transaction amount", example = "49.99")
    private BigDecimal amount;
    @Schema(description = "Income or Expense", example = "EXPENSE")
    private TransactionType type;
    @Schema(description = "Category name", example = "Food")
    private String category;
    @Schema(description = "Optional description", example = "Lunch at cafe")
    private String description;
    @Schema(description = "Whether the transaction is flagged for fraud", example = "false")
    private boolean isFlagged;
    @Schema(description = "Creation timestamp", example = "2026-03-18T22:00:00Z")
    private LocalDateTime createdAt;
}
