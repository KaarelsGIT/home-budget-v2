package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.Role;
import ee.kaarel.homebudgetappv2.model.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
        Long id,
        String username,
        Role role,
        UserStatus status,
        LocalDateTime createdAt,
        Long parentId,
        UUID familyId
) {
}
