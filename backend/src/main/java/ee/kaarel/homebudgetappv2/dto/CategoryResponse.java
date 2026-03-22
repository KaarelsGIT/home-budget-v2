package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.CategoryGroup;

import java.util.List;
import java.util.UUID;

public record CategoryResponse(
        Long id,
        String name,
        CategoryGroup group,
        UUID ownerFamilyId,
        List<SubCategoryResponse> subCategories
) {
}
