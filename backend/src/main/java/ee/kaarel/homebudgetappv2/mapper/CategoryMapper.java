package ee.kaarel.homebudgetappv2.mapper;

import ee.kaarel.homebudgetappv2.dto.CategoryDTO;
import ee.kaarel.homebudgetappv2.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "userId", source = "user.id")
    CategoryDTO toDto(Category category);
}
