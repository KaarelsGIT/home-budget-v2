package ee.kaarel.homebudgetappv2.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountResponse {
    private Long id;
    private String name;
    private BigDecimal balance;
    private String currency;
    private Long userId;
}
