package ee.kaarel.homebudgetappv2.dto;

import java.math.BigDecimal;

public record StatsMonthlyItem(
        int month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}
