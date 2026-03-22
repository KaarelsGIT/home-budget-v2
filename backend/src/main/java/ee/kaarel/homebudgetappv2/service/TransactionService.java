package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.CreateTransactionRequest;
import ee.kaarel.homebudgetappv2.dto.TransactionDTO;
import ee.kaarel.homebudgetappv2.dto.TransactionFilterRequest;
import ee.kaarel.homebudgetappv2.model.Account;
import ee.kaarel.homebudgetappv2.model.SubCategory;
import ee.kaarel.homebudgetappv2.model.Transaction;
import ee.kaarel.homebudgetappv2.model.TransactionType;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final UserAccessService userAccessService;
    private final LocalizationService localizationService;

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAll(TransactionFilterRequest filter) {
        return getScopedTransactions(filter).stream()
                .filter(transaction -> filter.type() == null || transaction.getType() == filter.type())
                .filter(transaction -> filter.subCategoryId() == null
                        || (transaction.getCategory() != null && transaction.getCategory().getId().equals(filter.subCategoryId())))
                .filter(transaction -> filter.accountId() == null || usesAccount(transaction, filter.accountId()))
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Transaction> getScopedTransactions(TransactionFilterRequest filter) {
        User current = userAccessService.getCurrentUser();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return resolveScopedTransactions(filter, current, sort);
    }

    @Transactional(readOnly = true)
    public TransactionDTO getById(Long id) {
        return toDto(getAccessibleTransaction(id));
    }

    @Transactional
    public TransactionDTO createTransaction(CreateTransactionRequest request) {
        User current = userAccessService.getCurrentUser();
        Transaction transaction = new Transaction();
        transaction.setCreatedBy(current);
        applyRequest(transaction, request, null);
        return toDto(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionDTO updateTransaction(Long id, CreateTransactionRequest request) {
        Transaction transaction = getAccessibleTransaction(id);
        applyRequest(transaction, request, transaction.getId());
        return toDto(transactionRepository.save(transaction));
    }

    @Transactional
    public void deleteTransaction(Long id) {
        transactionRepository.delete(getAccessibleTransaction(id));
    }

    private List<Transaction> resolveScopedTransactions(TransactionFilterRequest filter, User current, Sort sort) {
        if (filter.startDate() != null || filter.endDate() != null) {
            LocalDateTime start = filter.startDate() == null
                    ? LocalDateTime.of(1970, 1, 1, 0, 0)
                    : filter.startDate().atStartOfDay();
            LocalDateTime end = filter.endDate() == null
                    ? LocalDateTime.now().with(LocalTime.MAX)
                    : filter.endDate().atTime(LocalTime.MAX);
            if (userAccessService.isAdmin(current)) {
                return transactionRepository.findAll().stream()
                        .filter(t -> !t.getCreatedAt().isBefore(start) && !t.getCreatedAt().isAfter(end))
                        .toList();
            }
            if (userAccessService.isChild(current)) {
                return transactionRepository.findAllByOwnerIdAndCreatedAtBetween(current.getId(), start, end, sort);
            }
            if (filter.userId() != null) {
                User scopedUser = userAccessService.getAccessibleUserOrThrow(filter.userId());
                return transactionRepository.findAllByOwnerIdAndCreatedAtBetween(scopedUser.getId(), start, end, sort);
            }
            return transactionRepository.findAllByFamilyIdAndCreatedAtBetween(current.getFamilyId(), start, end, sort);
        }
        if (userAccessService.isAdmin(current)) {
            return transactionRepository.findAll(sort);
        }
        if (userAccessService.isChild(current)) {
            return transactionRepository.findAllByOwnerId(current.getId(), sort);
        }
        if (filter.userId() != null) {
            User scopedUser = userAccessService.getAccessibleUserOrThrow(filter.userId());
            return transactionRepository.findAllByOwnerId(scopedUser.getId(), sort);
        }
        return transactionRepository.findAllByFamilyId(current.getFamilyId(), sort);
    }

    private boolean usesAccount(Transaction transaction, Long accountId) {
        return (transaction.getFromAccount() != null && transaction.getFromAccount().getId().equals(accountId))
                || (transaction.getToAccount() != null && transaction.getToAccount().getId().equals(accountId));
    }

    private void applyRequest(Transaction transaction, CreateTransactionRequest request, Long transactionIdToExclude) {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.transaction.amount"));
        }

        transaction.setAmount(request.amount());
        transaction.setType(request.type());

        SubCategory subCategory = request.subCategoryId() == null ? null : categoryService.getAccessibleSubCategory(request.subCategoryId());
        transaction.setCategory(subCategory);

        switch (request.type()) {
            case INCOME -> applyIncome(transaction, request);
            case EXPENSE -> applyExpense(transaction, request, transactionIdToExclude);
            case TRANSFER -> applyTransfer(transaction, request, transactionIdToExclude);
        }
    }

    private void applyIncome(Transaction transaction, CreateTransactionRequest request) {
        if (request.toAccountId() == null || request.fromAccountId() != null) {
            throw new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.transaction.incomeShape"));
        }
        transaction.setFromAccount(null);
        transaction.setToAccount(accountService.getAccessibleAccountOrThrow(request.toAccountId()));
    }

    private void applyExpense(Transaction transaction, CreateTransactionRequest request, Long excludeId) {
        if (request.fromAccountId() == null || request.toAccountId() != null) {
            throw new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.transaction.expenseShape"));
        }
        Account fromAccount = accountService.getAccessibleAccountOrThrow(request.fromAccountId());
        ensureCanSpend(fromAccount, request.amount(), excludeId);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(null);
    }

    private void applyTransfer(Transaction transaction, CreateTransactionRequest request, Long excludeId) {
        if (request.fromAccountId() == null || request.toAccountId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.transaction.transferShape"));
        }
        Account fromAccount = accountService.getAccessibleAccountOrThrow(request.fromAccountId());
        Account toAccount = accountService.getAccessibleAccountOrThrow(request.toAccountId());
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.transaction.transferSameAccount"));
        }
        if (!fromAccount.getOwner().getFamilyId().equals(toAccount.getOwner().getFamilyId())) {
            throw new ResponseStatusException(FORBIDDEN, localizationService.getMessage("error.family.accessDenied"));
        }
        ensureCanSpend(fromAccount, request.amount(), excludeId);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setCategory(request.subCategoryId() == null ? null : categoryService.getAccessibleSubCategory(request.subCategoryId()));
    }

    private void ensureCanSpend(Account account, BigDecimal amount, Long excludeId) {
        BigDecimal balance = excludeId == null
                ? accountService.calculateBalance(account.getId())
                : accountService.calculateBalanceExcluding(account.getId(), excludeId);
        if (balance.compareTo(amount) < 0) {
            throw new ResponseStatusException(FORBIDDEN, localizationService.getMessage("error.account.insufficientBalance"));
        }
    }

    private Transaction getAccessibleTransaction(Long id) {
        User current = userAccessService.getCurrentUser();
        Transaction transaction = userAccessService.isAdmin(current)
                ? transactionRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.transaction.notFound")))
                : userAccessService.isChild(current)
                    ? transactionRepository.findByIdAndOwnerId(id, current.getId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.transaction.notFound")))
                    : transactionRepository.findByIdAndFamilyId(id, current.getFamilyId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.transaction.notFound")));
        if (!userAccessService.canAccessTransaction(current, transaction)) {
            throw new ResponseStatusException(FORBIDDEN, localizationService.getMessage("error.transaction.accessDenied"));
        }
        return transaction;
    }

    private TransactionDTO toDto(Transaction transaction) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getFromAccount() == null ? null : transaction.getFromAccount().getId(),
                transaction.getFromAccount() == null ? null : transaction.getFromAccount().getName(),
                transaction.getToAccount() == null ? null : transaction.getToAccount().getId(),
                transaction.getToAccount() == null ? null : transaction.getToAccount().getName(),
                transaction.getCategory() == null ? null : transaction.getCategory().getId(),
                transaction.getCategory() == null ? null : transaction.getCategory().getName(),
                transaction.getCategory() == null ? null : transaction.getCategory().getParentCategory().getName(),
                transaction.getCreatedAt(),
                transaction.getCreatedBy().getId(),
                transaction.getCreatedBy().getUsername()
        );
    }
}
