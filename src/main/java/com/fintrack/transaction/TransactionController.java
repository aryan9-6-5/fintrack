package com.fintrack.transaction;

import com.fintrack.transaction.dto.CategorySummaryResponse;
import com.fintrack.transaction.dto.TransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.fintrack.common.exception.ApiErrorResponse;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for managing transactions")
public class TransactionController {

    private final TransactionService transactionService;

    private String getAuthenticatedUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping
    @Operation(summary = "Create transaction", description = "Records a new income or expense. Automatically flags for fraud if > 3x average.")
    @ApiResponse(responseCode = "201", description = "Transaction created")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return new ResponseEntity<>(transactionService.create(getAuthenticatedUserEmail(), request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List transactions", description = "Returns all transactions for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(transactionService.getAll(getAuthenticatedUserEmail()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction", description = "Fetches details of a single transaction by ID.")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(id, getAuthenticatedUserEmail()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction")
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<TransactionResponse> update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(id, getAuthenticatedUserEmail(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id, getAuthenticatedUserEmail());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Get category summary", description = "Returns totals grouped by category with optional date filtering.")
    @ApiResponse(responseCode = "200", description = "Success")
    @Tag(name = "Summary")
    public ResponseEntity<List<CategorySummaryResponse>> getSummary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(transactionService.getSummary(getAuthenticatedUserEmail(), month, year));
    }
}
