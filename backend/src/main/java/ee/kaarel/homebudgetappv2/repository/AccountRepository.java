package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByOwnerFamilyIdOrderByNameAsc(UUID familyId);
    List<Account> findByOwnerIdOrderByNameAsc(Long ownerId);
    Optional<Account> findByIdAndOwnerFamilyId(Long id, UUID familyId);
    Optional<Account> findByIdAndOwnerId(Long id, Long ownerId);
}
