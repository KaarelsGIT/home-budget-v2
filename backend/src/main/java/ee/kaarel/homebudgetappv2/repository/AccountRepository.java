package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByOrderByUserIdAscNameAsc();
    List<Account> findByUserIdIn(Collection<Long> userIds);
    List<Account> findByUserIdOrderByNameAsc(Long userId);
    Optional<Account> findByIdAndUserIdIn(Long id, Collection<Long> userIds);
}
