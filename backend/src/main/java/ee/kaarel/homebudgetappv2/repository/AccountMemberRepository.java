package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.AccountMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountMemberRepository extends JpaRepository<AccountMember, Long> {
    List<AccountMember> findByAccountId(Long accountId);
    void deleteByAccountId(Long accountId);
}
