package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    Optional<SubCategory> findByIdAndParentCategoryOwnerFamilyId(Long id, UUID familyId);
}
