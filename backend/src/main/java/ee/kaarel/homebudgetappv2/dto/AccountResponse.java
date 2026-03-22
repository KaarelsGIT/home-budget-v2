package ee.kaarel.homebudgetappv2.dto;

import java.math.BigDecimal;
import java.util.List;

public record AccountResponse(
        Long id,
        String name,
        Long ownerId,
        String ownerUsername,
        boolean isDefault,
        BigDecimal balance,
        List<AccountMemberResponse> members
) {
}
