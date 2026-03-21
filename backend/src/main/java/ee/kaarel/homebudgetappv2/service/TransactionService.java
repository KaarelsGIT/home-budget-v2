package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.TransactionRequest;
import ee.kaarel.homebudgetappv2.dto.TransactionDTO;
import ee.kaarel.homebudgetappv2.dto.TransferRequest;
import ee.kaarel.homebudgetappv2.mapper.TransactionMapper;
import ee.kaarel.homebudgetappv2.model.Account;
import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.Transaction;
import ee.kaarel.homebudgetappv2.model.TransactionType;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAll() {
        Specification<Transaction> spec = TransactionSpecifications.userIdIn(userAccessService.getAccessibleUserIds());
        return transactionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(transactionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionDTO getById(Long id) {
        Transaction transaction = transactionRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Transaction not found"));
        return transactionMapper.toDto(transaction);
    }

    @Transactional
    public TransactionDTO create(TransactionRequest request, Long userId) {
        User targetUser = userAccessService.resolveTargetUser(userId);

        Transaction transaction = new Transaction();
        transaction.setUser(targetUser);
        applyRequestToTransaction(transaction, request, targetUser);

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toDto(saved);
    }

    @Transactional
    public TransactionDTO update(Long id, TransactionRequest request) {
        Transaction existing = transactionRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Transaction not found"));

        rollbackBalanceImpact(existing);
        applyRequestToTransaction(existing, request, existing.getUser());

        Transaction saved = transactionRepository.save(existing);
        return transactionMapper.toDto(saved);
    }

    @Transactional
    public TransactionDTO transfer(TransferRequest request) {
        Transaction transaction = new Transaction();
        transaction.setUser(userAccessService.getCurrentUser());

        applyTransfer(transaction, request.getAmount(), request.getFromAccountId(), request.getToAccountId(), true);

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Transaction transaction = transactionRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Transaction not found"));

        rollbackBalanceImpact(transaction);
        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> filter(LocalDate startDate,
                                       LocalDate endDate,
                                       Long categoryId,
                                       TransactionType type,
                                       Long accountId,
                                       String sortBy,
                                       String direction) {
        String sortField = resolveSortField(sortBy);
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Specification<Transaction> spec = Specification.where(TransactionSpecifications.userIdIn(userAccessService.getAccessibleUserIds()))
                .and(TransactionSpecifications.dateFrom(startDate))
                .and(TransactionSpecifications.dateTo(endDate))
                .and(TransactionSpecifications.category(categoryId))
                .and(TransactionSpecifications.type(type))
                .and(TransactionSpecifications.account(accountId));

        return transactionRepository.findAll(spec, Sort.by(sortDirection, sortField))
                .stream()
                .map(transactionMapper::toDto)
                .toList();
    }

    private void applyRequestToTransaction(Transaction transaction, TransactionRequest request, User owner) {
        validateTypeRules(request);

        Category category = request.getCategoryId() == null ? null : categoryService.getAccessibleCategoryOrThrow(request.getCategoryId());
        if (category != null && !category.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Category must belong to transaction owner");
        }

        if (request.getType() != TransactionType.TRANSFER && category == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Category is required for INCOME and EXPENSE");
        }

        if (request.getType() == TransactionType.TRANSFER && category != null) {
            throw new ResponseStatusException(BAD_REQUEST, "Category must be null for TRANSFER");
        }

        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());

        switch (request.getType()) {
            case INCOME -> applyIncome(transaction, request.getAmount(), request.getToAccountId());
            case EXPENSE -> applyExpense(transaction, request.getAmount(), request.getFromAccountId());
            case TRANSFER -> applyTransfer(transaction, request.getAmount(), request.getFromAccountId(), request.getToAccountId(), true);
        }
    }

    private void applyIncome(Transaction transaction, BigDecimal amount, Long toAccountId) {
        Account toAccount = accountService.getAccessibleAccountOrThrow(toAccountId);
        transaction.setType(TransactionType.INCOME);
        transaction.setFromAccount(null);
        transaction.setToAccount(toAccount);
        toAccount.setBalance(toAccount.getBalance().add(amount));
    }

    private void applyExpense(Transaction transaction, BigDecimal amount, Long fromAccountId) {
        Account fromAccount = accountService.getAccessibleAccountOrThrow(fromAccountId);
        ensureSufficientBalance(fromAccount, amount);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory(transaction.getCategory());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(null);
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
    }

    private void applyTransfer(Transaction transaction,
                               BigDecimal amount,
                               Long fromAccountId,
                               Long toAccountId,
                               boolean requireOwnedSource) {
        Account fromAccount = requireOwnedSource
                ? accountService.getOwnedAccountOrThrow(fromAccountId)
                : accountService.getAccessibleAccountOrThrow(fromAccountId);
        Account toAccount = accountService.getAccessibleAccountOrThrow(toAccountId);

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Transfer accounts must be different");
        }
        if (requireOwnedSource && !fromAccount.getUser().getId().equals(userAccessService.getCurrentUser().getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Source account must belong to the current user");
        }

        ensureSufficientBalance(fromAccount, amount);

        transaction.setType(TransactionType.TRANSFER);
        transaction.setCategory(null);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
    }

    private void validateTypeRules(TransactionRequest request) {
        TransactionType type = request.getType();

        switch (type) {
            case INCOME -> {
                if (request.getToAccountId() == null || request.getFromAccountId() != null) {
                    throw new ResponseStatusException(BAD_REQUEST, "INCOME requires toAccount and no fromAccount");
                }
            }
            case EXPENSE -> {
                if (request.getFromAccountId() == null || request.getToAccountId() != null) {
                    throw new ResponseStatusException(BAD_REQUEST, "EXPENSE requires fromAccount and no toAccount");
                }
            }
            case TRANSFER -> {
                if (request.getFromAccountId() == null || request.getToAccountId() == null) {
                    throw new ResponseStatusException(BAD_REQUEST, "TRANSFER requires both fromAccount and toAccount");
                }
            }
        }
    }

    private void rollbackBalanceImpact(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();

        switch (transaction.getType()) {
            case INCOME -> transaction.getToAccount().setBalance(transaction.getToAccount().getBalance().subtract(amount));
            case EXPENSE -> transaction.getFromAccount().setBalance(transaction.getFromAccount().getBalance().add(amount));
            case TRANSFER -> {
                transaction.getFromAccount().setBalance(transaction.getFromAccount().getBalance().add(amount));
                transaction.getToAccount().setBalance(transaction.getToAccount().getBalance().subtract(amount));
            }
        }
    }

    private void ensureSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(FORBIDDEN, "Insufficient balance");
        }
    }

    private String resolveSortField(String sortBy) {
        if ("amount".equalsIgnoreCase(sortBy)) {
            return "amount";
        }
        return "createdAt";
    }
}
