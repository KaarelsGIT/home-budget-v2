package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal balance;

    @NotNull
    private AccountType type;

    private Long parentAccountId;
}
