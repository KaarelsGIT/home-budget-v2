package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.AccountMemberRole;
import jakarta.validation.constraints.NotNull;

public record AccountMemberRequest(
        @NotNull Long userId,
        @NotNull AccountMemberRole role
) {
}
