package com.fintrack.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Summary of transactions by category")
public class CategorySummaryResponse {
    @Schema(description = "Category name", example = "Entertainment")
    private String category;
    @Schema(description = "Total amount for this category", example = "150.00")
    private BigDecimal totalAmount;
}
