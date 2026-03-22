package ee.kaarel.homebudgetappv2.dto;

import jakarta.validation.constraints.NotNull;

public record MarkRecurringPaymentPaidRequest(@NotNull Long transactionId) {
}
