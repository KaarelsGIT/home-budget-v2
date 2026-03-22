package ee.kaarel.homebudgetappv2.dto;

import java.math.BigDecimal;
import java.util.List;

public record StatsSummaryResponse(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net,
        List<UserStatsItem> perUser
) {
}
