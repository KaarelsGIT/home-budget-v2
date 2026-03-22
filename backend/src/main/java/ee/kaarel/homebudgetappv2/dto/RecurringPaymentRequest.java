package ee.kaarel.homebudgetappv2.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RecurringPaymentRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal amount,
        @NotNull Long subCategoryId,
        @NotNull @Min(1) @Max(31) Integer dueDay,
        Boolean active,
        Long ownerId
) {
}
