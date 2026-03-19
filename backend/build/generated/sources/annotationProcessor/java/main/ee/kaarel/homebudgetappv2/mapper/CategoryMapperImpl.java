package ee.kaarel.homebudgetappv2.mapper;

import ee.kaarel.homebudgetappv2.dto.CategoryResponse;
import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-19T18:14:04+0200",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.1.jar, environment: Java 21.0.7 (Homebrew)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryResponse toResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse categoryResponse = new CategoryResponse();

        categoryResponse.setParentId( categoryParentId( category ) );
        categoryResponse.setUserId( categoryUserId( category ) );
        categoryResponse.setId( category.getId() );
        categoryResponse.setName( category.getName() );
        categoryResponse.setType( category.getType() );

        return categoryResponse;
    }

    private Long categoryParentId(Category category) {
        Category parent = category.getParent();
        if ( parent == null ) {
            return null;
        }
        return parent.getId();
    }

    private Long categoryUserId(Category category) {
        User user = category.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }
}
