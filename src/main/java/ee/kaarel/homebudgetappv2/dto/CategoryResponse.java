package ee.kaarel.homebudgetappv2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private Long userId;
}
