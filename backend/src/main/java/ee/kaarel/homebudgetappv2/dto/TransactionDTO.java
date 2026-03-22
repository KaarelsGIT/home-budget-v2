package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(
        Long id,
        BigDecimal amount,
        TransactionType type,
        Long fromAccountId,
        String fromAccountName,
        Long toAccountId,
        String toAccountName,
        Long subCategoryId,
        String subCategoryName,
        String categoryName,
        LocalDateTime createdAt,
        Long createdById,
        String createdByUsername
) {
}
