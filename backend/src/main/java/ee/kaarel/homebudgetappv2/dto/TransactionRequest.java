package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.TransactionType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionRequest {

    @NotNull
    private TransactionType type;

    @NotNull
    @Positive
    private BigDecimal amount;
    private Long categoryId;
    private Long fromAccountId;
    private Long toAccountId;
}
