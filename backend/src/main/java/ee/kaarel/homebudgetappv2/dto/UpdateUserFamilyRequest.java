package ee.kaarel.homebudgetappv2.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateUserFamilyRequest(@NotNull UUID familyId) {
}
