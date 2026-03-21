package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CategoryTreeDto {
    private Long id;
    private String name;
    private CategoryType type;
    private Long parentId;
    private List<CategoryTreeDto> children;
}
