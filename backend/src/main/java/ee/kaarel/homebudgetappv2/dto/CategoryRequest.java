package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank
    private String name;

    @NotNull
    private CategoryType type;

    private Long parentId;
}
