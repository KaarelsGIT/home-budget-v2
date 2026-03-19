package ee.kaarel.homebudgetappv2.mapper;

import ee.kaarel.homebudgetappv2.dto.CategoryResponse;
import ee.kaarel.homebudgetappv2.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "userId", source = "user.id")
    CategoryResponse toResponse(Category category);
}
