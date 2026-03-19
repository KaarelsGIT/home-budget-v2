package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.CategoryType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
    private Long parentId;
    private Long userId;
}
