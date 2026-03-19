package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdIn(Collection<Long> userIds);
    List<Category> findByUserIdInAndType(Collection<Long> userIds, CategoryType type);
    Optional<Category> findByIdAndUserIdIn(Long id, Collection<Long> userIds);
}
