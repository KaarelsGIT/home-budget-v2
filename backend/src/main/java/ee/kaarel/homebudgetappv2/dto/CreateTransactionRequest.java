package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull TransactionType type,
        @NotNull @Positive BigDecimal amount,
        Long fromAccountId,
        Long toAccountId,
        Long subCategoryId
) {
}
