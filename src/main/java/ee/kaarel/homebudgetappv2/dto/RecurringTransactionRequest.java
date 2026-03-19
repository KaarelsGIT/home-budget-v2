package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.RecurringFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RecurringTransactionRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private Long categoryId;

    @NotNull
    private RecurringFrequency frequency;

    @NotNull
    private LocalDate nextExecutionDate;

    @NotNull
    private Boolean active;

    private Long accountId;
}
