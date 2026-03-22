package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.CategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByOwnerFamilyIdOrderByNameAsc(UUID familyId);
    List<Category> findByOwnerFamilyIdAndGroupOrderByNameAsc(UUID familyId, CategoryGroup group);
    Optional<Category> findByIdAndOwnerFamilyId(Long id, UUID familyId);
}
