package ee.kaarel.homebudgetappv2.dto;

import java.math.BigDecimal;

public record StatsCategoryItem(
        String categoryName,
        String subCategoryName,
        BigDecimal amount
) {
}
