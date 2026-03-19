package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.model.Transaction;
import ee.kaarel.homebudgetappv2.model.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collection;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> userIdIn(Collection<Long> userIds) {
        return (root, query, cb) -> root.get("user").get("id").in(userIds);
    }

    public static Specification<Transaction> dateFrom(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("date"), startDate);
    }

    public static Specification<Transaction> dateTo(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("date"), endDate);
    }

    public static Specification<Transaction> category(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? cb.conjunction() : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> type(TransactionType type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> account(Long accountId) {
        return (root, query, cb) -> {
            if (accountId == null) {
                return cb.conjunction();
            }
            return cb.or(
                    cb.equal(root.get("fromAccount").get("id"), accountId),
                    cb.equal(root.get("toAccount").get("id"), accountId)
            );
        };
    }
}
