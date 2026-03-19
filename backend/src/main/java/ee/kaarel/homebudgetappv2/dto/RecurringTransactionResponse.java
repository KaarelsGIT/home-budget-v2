package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.RecurringFrequency;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RecurringTransactionResponse {
    private Long id;
    private BigDecimal amount;
    private Long categoryId;
    private RecurringFrequency frequency;
    private LocalDate nextExecutionDate;
    private boolean active;
    private Long accountId;
    private Long userId;
}
