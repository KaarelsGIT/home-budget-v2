package ee.kaarel.homebudgetappv2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FamilyOverviewResponse {
    private int year;
    private Integer month;
    private OverviewTotals totals;
    private List<OverviewMonthlyItem> monthly;
    private List<OverviewCategoryItem> categories;
    private List<TransactionResponse> transactions;
}
