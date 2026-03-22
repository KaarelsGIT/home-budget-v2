package ee.kaarel.homebudgetappv2.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AccountRequest(
        @NotBlank String name,
        Boolean isDefault,
        Long ownerId,
        List<AccountMemberRequest> members
) {
}
