package ee.kaarel.homebudgetappv2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TransferResponse {
    private Long transactionId;
    private Long fromAccountId;
    private BigDecimal fromAccountBalance;
    private Long toAccountId;
    private String toAccountName;
}
