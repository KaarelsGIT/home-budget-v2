package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUserIdIn(Collection<Long> userIds);
    Optional<RecurringTransaction> findByIdAndUserIdIn(Long id, Collection<Long> userIds);
    List<RecurringTransaction> findByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);
}
