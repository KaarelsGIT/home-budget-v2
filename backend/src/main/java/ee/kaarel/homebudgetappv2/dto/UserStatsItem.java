package ee.kaarel.homebudgetappv2.dto;

import java.math.BigDecimal;

public record UserStatsItem(
        Long userId,
        String username,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}
