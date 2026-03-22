package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.CategoryGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(
        @NotBlank String name,
        @NotNull CategoryGroup group
) {
}
