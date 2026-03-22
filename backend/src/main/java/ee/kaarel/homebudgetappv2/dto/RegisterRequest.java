package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotNull Role role,
        Long parentId
) {
}
