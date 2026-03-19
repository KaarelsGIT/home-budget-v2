package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.RecurringTransactionRequest;
import ee.kaarel.homebudgetappv2.dto.RecurringTransactionResponse;
import ee.kaarel.homebudgetappv2.mapper.RecurringTransactionMapper;
import ee.kaarel.homebudgetappv2.model.*;
import ee.kaarel.homebudgetappv2.repository.AccountRepository;
import ee.kaarel.homebudgetappv2.repository.RecurringTransactionRepository;
import ee.kaarel.homebudgetappv2.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final UserAccessService userAccessService;
    private final RecurringTransactionMapper recurringTransactionMapper;

    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getAll() {
        return recurringTransactionRepository.findByUserIdIn(userAccessService.getAccessibleUserIds())
                .stream()
                .map(recurringTransactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecurringTransactionResponse getById(Long id) {
        RecurringTransaction recurring = getAccessibleRecurringOrThrow(id);
        return recurringTransactionMapper.toResponse(recurring);
    }

    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest request, Long userId) {
        User targetUser = userAccessService.resolveTargetUser(userId);

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUser(targetUser);
        applyRequest(recurring, request, targetUser);

        return recurringTransactionMapper.toResponse(recurringTransactionRepository.save(recurring));
    }

    @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest request) {
        RecurringTransaction recurring = getAccessibleRecurringOrThrow(id);
        applyRequest(recurring, request, recurring.getUser());

        return recurringTransactionMapper.toResponse(recurringTransactionRepository.save(recurring));
    }

    @Transactional
    public void delete(Long id) {
        RecurringTransaction recurring = getAccessibleRecurringOrThrow(id);
        recurringTransactionRepository.delete(recurring);
    }

    @Transactional
    public void executeDueRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueItems = recurringTransactionRepository.findByActiveTrueAndNextExecutionDateLessThanEqual(today);

        for (RecurringTransaction recurring : dueItems) {
            while (!recurring.getNextExecutionDate().isAfter(today)) {
                createTransactionFromRecurring(recurring);
                recurring.setNextExecutionDate(incrementDate(recurring.getNextExecutionDate(), recurring.getFrequency()));
            }
            recurringTransactionRepository.save(recurring);
        }
    }

    private void applyRequest(RecurringTransaction recurring, RecurringTransactionRequest request, User owner) {
        Category category = request.getCategoryId() == null ? null : categoryService.getAccessibleCategoryOrThrow(request.getCategoryId());
        if (category != null && !category.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Category must belong to recurring owner");
        }

        Account account = request.getAccountId() == null ? null : accountRepository.findByIdAndUserIdIn(request.getAccountId(), userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));

        if (account != null && !account.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Account must belong to recurring owner");
        }

        if (Boolean.TRUE.equals(request.getActive()) && account == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Active recurring transaction requires account");
        }

        recurring.setAmount(request.getAmount());
        recurring.setCategory(category);
        recurring.setFrequency(request.getFrequency());
        recurring.setNextExecutionDate(request.getNextExecutionDate());
        recurring.setActive(request.getActive());
        recurring.setAccount(account);
    }

    private void createTransactionFromRecurring(RecurringTransaction recurring) {
        if (recurring.getAccount() == null) {
            return;
        }

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.EXPENSE);
        transaction.setAmount(recurring.getAmount());
        transaction.setDate(recurring.getNextExecutionDate());
        transaction.setDescription("Auto-generated recurring payment");
        transaction.setUser(recurring.getUser());
        transaction.setCategory(recurring.getCategory());
        transaction.setFromAccount(recurring.getAccount());
        transaction.setToAccount(null);

        recurring.getAccount().setBalance(recurring.getAccount().getBalance().subtract(recurring.getAmount()));
        accountRepository.save(recurring.getAccount());
        transactionRepository.save(transaction);
    }

    private LocalDate incrementDate(LocalDate current, RecurringFrequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
        };
    }

    private RecurringTransaction getAccessibleRecurringOrThrow(Long id) {
        return recurringTransactionRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Recurring transaction not found"));
    }
}
