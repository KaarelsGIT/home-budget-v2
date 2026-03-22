package ee.kaarel.homebudgetappv2.dto;

import java.math.BigDecimal;

public record RecurringPaymentNotificationResponse(
        Long recurringPaymentId,
        String name,
        BigDecimal amount,
        Integer dueDay,
        Long subCategoryId,
        String subCategoryName,
        String categoryName,
        int year,
        int month,
        boolean paid,
        Long paidTransactionId
) {
}
