package ee.kaarel.homebudgetappv2.dto;

public record SubCategoryResponse(
        Long id,
        String name,
        Long parentCategoryId,
        String parentCategoryName
) {
}
