package ee.kaarel.homebudgetappv2.repository;

import ee.kaarel.homebudgetappv2.model.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            select distinct t from Transaction t
            left join t.fromAccount fa
            left join t.toAccount ta
            where fa.owner.familyId = :familyId or ta.owner.familyId = :familyId
            """)
    List<Transaction> findAllByFamilyId(@Param("familyId") UUID familyId, Sort sort);

    @Query("""
            select distinct t from Transaction t
            left join t.fromAccount fa
            left join t.toAccount ta
            where fa.owner.id = :ownerId or ta.owner.id = :ownerId or t.createdBy.id = :ownerId
            """)
    List<Transaction> findAllByOwnerId(@Param("ownerId") Long ownerId, Sort sort);

    @Query("""
            select distinct t from Transaction t
            left join t.fromAccount fa
            left join t.toAccount ta
            where (fa.owner.familyId = :familyId or ta.owner.familyId = :familyId)
              and t.createdAt between :start and :end
            """)
    List<Transaction> findAllByFamilyIdAndCreatedAtBetween(
            @Param("familyId") UUID familyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Sort sort
    );

    @Query("""
            select distinct t from Transaction t
            left join t.fromAccount fa
            left join t.toAccount ta
            where (fa.owner.id = :ownerId or ta.owner.id = :ownerId or t.createdBy.id = :ownerId)
              and t.createdAt between :start and :end
            """)
    List<Transaction> findAllByOwnerIdAndCreatedAtBetween(
            @Param("ownerId") Long ownerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Sort sort
    );

    @Query("""
            select coalesce(sum(
                case
                    when t.type = ee.kaarel.homebudgetappv2.model.TransactionType.INCOME and t.toAccount.id = :accountId then t.amount
                    when t.type = ee.kaarel.homebudgetappv2.model.TransactionType.EXPENSE and t.fromAccount.id = :accountId then -t.amount
                    when t.type = ee.kaarel.homebudgetappv2.model.TransactionType.TRANSFER and t.toAccount.id = :accountId then t.amount
                    when t.type = ee.kaarel.homebudgetappv2.model.TransactionType.TRANSFER and t.fromAccount.id = :accountId then -t.amount
                    else 0
                end
            ), 0)
            from Transaction t
            where (t.fromAccount.id = :accountId or t.toAccount.id = :accountId)
            """)
    BigDecimal calculateBalance(@Param("accountId") Long accountId);

    @Query("""
            select coalesce(sum(
                case
                    when t.type = ee.kaarel.homebudgetappv2.model.TransactionType.INCOME and t.toAccount.id = :accountId then t.amount
                    when t.type = ee.kaarel.homebudgetappv2.model.TransactionType.EXPENSE and t.fromAccount.id = :accountId then -t.amount
                    when t.type = ee.kaarel.homebudgetappv2.model.TransactionType.TRANSFER and t.toAccount.id = :accountId then t.amount
                    when t.type = ee.kaarel.homebudgetappv2.model.TransactionType.TRANSFER and t.fromAccount.id = :accountId then -t.amount
                    else 0
                end
            ), 0)
            from Transaction t
            where (t.fromAccount.id = :accountId or t.toAccount.id = :accountId)
              and (:excludeId is null or t.id <> :excludeId)
            """)
    BigDecimal calculateBalanceExcluding(@Param("accountId") Long accountId, @Param("excludeId") Long excludeId);

    @Query("""
            select distinct t from Transaction t
            left join t.fromAccount fa
            left join t.toAccount ta
            where t.id = :id and (fa.owner.familyId = :familyId or ta.owner.familyId = :familyId)
            """)
    Optional<Transaction> findByIdAndFamilyId(@Param("id") Long id, @Param("familyId") UUID familyId);

    @Query("""
            select distinct t from Transaction t
            left join t.fromAccount fa
            left join t.toAccount ta
            where t.id = :id and (fa.owner.id = :ownerId or ta.owner.id = :ownerId or t.createdBy.id = :ownerId)
            """)
    Optional<Transaction> findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);
}
