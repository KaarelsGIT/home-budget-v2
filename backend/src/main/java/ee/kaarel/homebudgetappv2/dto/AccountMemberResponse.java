package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.AccountMemberRole;

public record AccountMemberResponse(
        Long userId,
        String username,
        AccountMemberRole role
) {
}
