package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.Role;
import ee.kaarel.homebudgetappv2.model.UserStatus;

import java.util.UUID;

public record AuthResponse(
        String token,
        Long userId,
        String username,
        Role role,
        UserStatus status,
        UUID familyId
) {
}
