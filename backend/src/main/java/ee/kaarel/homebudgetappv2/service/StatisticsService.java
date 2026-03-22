package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.StatsCategoryItem;
import ee.kaarel.homebudgetappv2.dto.StatsMonthlyItem;
import ee.kaarel.homebudgetappv2.dto.StatsSummaryResponse;
import ee.kaarel.homebudgetappv2.dto.StatsTrendItem;
import ee.kaarel.homebudgetappv2.dto.TransactionFilterRequest;
import ee.kaarel.homebudgetappv2.dto.UserStatsItem;
import ee.kaarel.homebudgetappv2.model.Transaction;
import ee.kaarel.homebudgetappv2.model.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TransactionService transactionService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public StatsSummaryResponse getSummary(Integer year, Integer month, Long userId) {
        List<Transaction> transactions = resolveTransactions(year, month, userId);
        BigDecimal income = sum(transactions, TransactionType.INCOME);
        BigDecimal expense = sum(transactions, TransactionType.EXPENSE);
        List<UserStatsItem> perUser = userId == null
                ? userService.getUsers().stream()
                    .map(user -> {
                        List<Transaction> userTransactions = transactions.stream()
                                .filter(transaction -> transaction.getCreatedBy().getId().equals(user.id()))
                                .toList();
                        BigDecimal userIncome = sum(userTransactions, TransactionType.INCOME);
                        BigDecimal userExpense = sum(userTransactions, TransactionType.EXPENSE);
                        return new UserStatsItem(user.id(), user.username(), userIncome, userExpense, userIncome.subtract(userExpense));
                    })
                    .toList()
                : List.of();
        return new StatsSummaryResponse(income, expense, income.subtract(expense), perUser);
    }

    @Transactional(readOnly = true)
    public List<StatsMonthlyItem> getMonthly(int year, Long userId) {
        List<Transaction> transactions = resolveTransactions(year, null, userId);
        Map<Integer, List<Transaction>> byMonth = transactions.stream()
                .collect(Collectors.groupingBy(transaction -> transaction.getCreatedAt().getMonthValue()));
        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> {
                    List<Transaction> monthTransactions = byMonth.getOrDefault(month, List.of());
                    BigDecimal income = sum(monthTransactions, TransactionType.INCOME);
                    BigDecimal expense = sum(monthTransactions, TransactionType.EXPENSE);
                    return new StatsMonthlyItem(month, income, expense, income.subtract(expense));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StatsCategoryItem> getCategoryBreakdown(Integer year, Integer month, Long userId) {
        return resolveTransactions(year, month, userId).stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .filter(transaction -> transaction.getCategory() != null)
                .collect(Collectors.groupingBy(
                        transaction -> transaction.getCategory().getParentCategory().getName() + "::" + transaction.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    String[] names = entry.getKey().split("::", 2);
                    return new StatsCategoryItem(names[0], names[1], entry.getValue());
                })
                .sorted(Comparator.comparing(StatsCategoryItem::amount).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StatsTrendItem> getTrends(int year, Long userId) {
        return getMonthly(year, userId).stream()
                .map(item -> new StatsTrendItem(
                        Month.of(item.month()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                        item.income(),
                        item.expense(),
                        item.net()
                ))
                .toList();
    }

    private List<Transaction> resolveTransactions(Integer year, Integer month, Long userId) {
        int targetYear = year == null ? LocalDate.now().getYear() : year;
        LocalDate start = LocalDate.of(targetYear, month == null ? 1 : month, 1);
        LocalDate end = month == null
                ? LocalDate.of(targetYear, 12, 31)
                : start.withDayOfMonth(start.lengthOfMonth());
        return transactionService.getScopedTransactions(new TransactionFilterRequest(start, end, null, null, null, userId));
    }

    private BigDecimal sum(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
