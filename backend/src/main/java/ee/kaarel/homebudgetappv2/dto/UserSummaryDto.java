package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.Role;
import ee.kaarel.homebudgetappv2.model.UserStatus;

public record UserSummaryDto(
        Long id,
        String username,
        Role role,
        UserStatus status
) {
}
