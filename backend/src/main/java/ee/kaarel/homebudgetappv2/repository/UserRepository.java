package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByParentId(Long parentId);
}
