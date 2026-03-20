package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.TransactionDTO;
import ee.kaarel.homebudgetappv2.model.Account;
import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.Transaction;
import ee.kaarel.homebudgetappv2.model.TransactionType;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.AccountRepository;
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
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryService categoryService;
    private final UserAccessService userAccessService;

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAll() {
        Specification<Transaction> spec = TransactionSpecifications.userIdIn(userAccessService.getAccessibleUserIds());
        return transactionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "date"))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionDTO getById(Long id) {
        Transaction transaction = transactionRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Transaction not found"));
        return toDto(transaction);
    }

    @Transactional
    public TransactionDTO create(TransactionDTO request, Long userId) {
        User targetUser = userAccessService.resolveTargetUser(userId);

        Transaction transaction = new Transaction();
        transaction.setUser(targetUser);
        applyRequestToTransaction(transaction, request, targetUser);

        Transaction saved = transactionRepository.save(transaction);
        return toDto(saved);
    }

    @Transactional
    public TransactionDTO update(Long id, TransactionDTO request) {
        Transaction existing = transactionRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Transaction not found"));

        rollbackBalanceImpact(existing);

        applyRequestToTransaction(existing, request, existing.getUser());
        Transaction saved = transactionRepository.save(existing);
        return toDto(saved);
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
                .map(this::toDto)
                .toList();
    }

    private void applyRequestToTransaction(Transaction transaction, TransactionDTO request, User owner) {
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

        Account fromAccount = request.getFromAccountId() == null ? null : getAccessibleAccount(request.getFromAccountId());
        Account toAccount = request.getToAccountId() == null ? null : getAccessibleAccount(request.getToAccountId());

        if (request.getType() == TransactionType.TRANSFER && fromAccount.getId().equals(toAccount.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Transfer accounts must be different");
        }

        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(category);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);

        applyBalanceImpact(transaction);
    }

    private Account getAccessibleAccount(Long accountId) {
        return accountRepository.findByIdAndUserIdIn(accountId, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
    }

    private void validateTypeRules(TransactionDTO request) {
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

    private void applyBalanceImpact(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();

        switch (transaction.getType()) {
            case INCOME -> transaction.getToAccount().setBalance(transaction.getToAccount().getBalance().add(amount));
            case EXPENSE -> transaction.getFromAccount().setBalance(transaction.getFromAccount().getBalance().subtract(amount));
            case TRANSFER -> {
                transaction.getFromAccount().setBalance(transaction.getFromAccount().getBalance().subtract(amount));
                transaction.getToAccount().setBalance(transaction.getToAccount().getBalance().add(amount));
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

    private String resolveSortField(String sortBy) {
        if ("amount".equalsIgnoreCase(sortBy)) {
            return "amount";
        }
        return "date";
    }

    private TransactionDTO toDto(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setDate(transaction.getDate());
        dto.setDescription(transaction.getDescription());
        dto.setUserId(transaction.getUser().getId());
        dto.setCategoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null);
        dto.setCategoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null);
        dto.setFromAccountId(transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null);
        dto.setToAccountId(transaction.getToAccount() != null ? transaction.getToAccount().getId() : null);
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
    }
}
