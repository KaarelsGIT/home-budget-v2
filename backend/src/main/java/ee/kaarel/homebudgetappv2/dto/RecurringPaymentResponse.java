package ee.kaarel.homebudgetappv2.dto;

import java.math.BigDecimal;

public record RecurringPaymentResponse(
        Long id,
        String name,
        BigDecimal amount,
        Long subCategoryId,
        String subCategoryName,
        String categoryName,
        Integer dueDay,
        Long ownerId,
        String ownerUsername,
        boolean active,
        boolean paidThisMonth,
        Long paidTransactionId
) {
}
