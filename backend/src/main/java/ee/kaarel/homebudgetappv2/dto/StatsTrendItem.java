package ee.kaarel.homebudgetappv2.dto;

import java.math.BigDecimal;

public record StatsTrendItem(
        String label,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}
