package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.RecurringPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecurringPaymentStatusRepository extends JpaRepository<RecurringPaymentStatus, Long> {
    Optional<RecurringPaymentStatus> findByRecurringPaymentIdAndYearAndMonth(Long recurringPaymentId, Integer year, Integer month);
    List<RecurringPaymentStatus> findByRecurringPaymentIdInAndYearAndMonth(List<Long> recurringPaymentIds, Integer year, Integer month);
}
