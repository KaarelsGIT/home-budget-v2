package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.*;
import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.Role;
import ee.kaarel.homebudgetappv2.model.Transaction;
import ee.kaarel.homebudgetappv2.model.TransactionType;
import ee.kaarel.homebudgetappv2.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserAccessService userAccessService;

    @Transactional(readOnly = true)
    public FamilyOverviewResponse getFamilyOverview(int year, Integer month) {
        if (userAccessService.getCurrentUser().getRole() != Role.PARENT) {
            throw new ResponseStatusException(FORBIDDEN, "Only parent users can access family overview");
        }
        if (month != null && (month < 1 || month > 12)) {
            throw new ResponseStatusException(BAD_REQUEST, "month must be between 1 and 12");
        }

        LocalDate from = LocalDate.of(year, month == null ? 1 : month, 1);
        LocalDate to = month == null
                ? LocalDate.of(year, 12, 31)
                : YearMonth.of(year, month).atEndOfMonth();

        Specification<Transaction> spec = Specification.where(TransactionSpecifications.userIdIn(userAccessService.getAccessibleUserIds()))
                .and(TransactionSpecifications.dateFrom(from))
                .and(TransactionSpecifications.dateTo(to));

        List<Transaction> transactions = transactionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        OverviewTotals totals = buildTotals(transactions);
        List<OverviewMonthlyItem> monthlyItems = buildMonthlyItems(transactions, year, month);
        List<OverviewCategoryItem> categoryItems = buildCategoryItems(transactions);
        List<TransactionDTO> rows = transactions.stream().map(this::toTransactionDto).toList();

        return new FamilyOverviewResponse(year, month, totals, monthlyItems, categoryItems, rows);
    }

    private OverviewTotals buildTotals(List<Transaction> transactions) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        BigDecimal transferOut = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                income = income.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                expense = expense.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.TRANSFER) {
                transferOut = transferOut.add(transaction.getAmount());
            }
        }

        return new OverviewTotals(income, expense, transferOut, income.subtract(expense));
    }

    private List<OverviewMonthlyItem> buildMonthlyItems(List<Transaction> transactions, int year, Integer monthFilter) {
        Map<Integer, BigDecimal> incomeByMonth = new LinkedHashMap<>();
        Map<Integer, BigDecimal> expenseByMonth = new LinkedHashMap<>();

        int startMonth = monthFilter == null ? 1 : monthFilter;
        int endMonth = monthFilter == null ? 12 : monthFilter;

        for (int month = startMonth; month <= endMonth; month++) {
            incomeByMonth.put(month, BigDecimal.ZERO);
            expenseByMonth.put(month, BigDecimal.ZERO);
        }

        for (Transaction transaction : transactions) {
            if (transaction.getCreatedAt().getYear() != year) {
                continue;
            }
            int month = transaction.getCreatedAt().getMonthValue();
            if (!incomeByMonth.containsKey(month)) {
                continue;
            }
            if (transaction.getType() == TransactionType.INCOME) {
                incomeByMonth.put(month, incomeByMonth.get(month).add(transaction.getAmount()));
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                expenseByMonth.put(month, expenseByMonth.get(month).add(transaction.getAmount()));
            }
        }

        List<OverviewMonthlyItem> result = new ArrayList<>();
        for (int month : incomeByMonth.keySet()) {
            BigDecimal income = incomeByMonth.get(month);
            BigDecimal expense = expenseByMonth.get(month);
            result.add(new OverviewMonthlyItem(month, income, expense, income.subtract(expense)));
        }

        return result;
    }

    private List<OverviewCategoryItem> buildCategoryItems(List<Transaction> transactions) {
        Map<String, BigDecimal> incomeByCategory = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();

        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.TRANSFER) {
                continue;
            }

            String key = resolveCategoryKey(transaction.getCategory());
            if (transaction.getType() == TransactionType.INCOME) {
                incomeByCategory.put(key, incomeByCategory.getOrDefault(key, BigDecimal.ZERO).add(transaction.getAmount()));
            } else {
                expenseByCategory.put(key, expenseByCategory.getOrDefault(key, BigDecimal.ZERO).add(transaction.getAmount()));
            }
        }

        Set<String> keys = new java.util.LinkedHashSet<>();
        keys.addAll(incomeByCategory.keySet());
        keys.addAll(expenseByCategory.keySet());

        return keys.stream()
                .sorted(Comparator.naturalOrder())
                .map(key -> {
                    String[] parts = key.split("::", 2);
                    return new OverviewCategoryItem(
                            parts[0],
                            parts[1],
                            incomeByCategory.getOrDefault(key, BigDecimal.ZERO),
                            expenseByCategory.getOrDefault(key, BigDecimal.ZERO)
                    );
                })
                .toList();
    }

    private String resolveCategoryKey(Category category) {
        if (category == null) {
            return "Uncategorized::Uncategorized";
        }
        return category.getName() + "::(total)";
    }

    private TransactionDTO toTransactionDto(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setUserId(transaction.getUser().getId());
        dto.setCategoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null);
        dto.setCategoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null);
        dto.setFromAccountId(transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null);
        dto.setToAccountId(transaction.getToAccount() != null ? transaction.getToAccount().getId() : null);
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}
