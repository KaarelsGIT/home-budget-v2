package ee.kaarel.homebudgetappv2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OverviewTotals {
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal transferOut;
    private BigDecimal net;
}
