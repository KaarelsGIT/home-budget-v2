package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.AccountType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountResponse {
    private Long id;
    private String name;
    private BigDecimal balance;
    private AccountType type;
    private Long userId;
    private Long parentAccountId;
}
