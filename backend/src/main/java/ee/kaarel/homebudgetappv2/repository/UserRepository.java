package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.Role;
import ee.kaarel.homebudgetappv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByRole(Role role);
    List<User> findByFamilyIdOrderByUsernameAsc(UUID familyId);
}
