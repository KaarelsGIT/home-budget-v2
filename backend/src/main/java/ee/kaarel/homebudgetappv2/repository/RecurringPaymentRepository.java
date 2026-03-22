package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.RecurringPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, Long> {
    List<RecurringPayment> findByOwnerFamilyIdOrderByNameAsc(UUID familyId);
    List<RecurringPayment> findByOwnerIdOrderByNameAsc(Long ownerId);
    Optional<RecurringPayment> findByIdAndOwnerFamilyId(Long id, UUID familyId);
    Optional<RecurringPayment> findByIdAndOwnerId(Long id, Long ownerId);
}
