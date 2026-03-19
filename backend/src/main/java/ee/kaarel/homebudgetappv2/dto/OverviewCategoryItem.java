package ee.kaarel.homebudgetappv2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OverviewCategoryItem {
    private String parentCategory;
    private String subCategory;
    private BigDecimal income;
    private BigDecimal expense;
}
