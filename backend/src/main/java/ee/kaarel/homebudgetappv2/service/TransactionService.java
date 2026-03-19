package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.TransactionRequest;
import ee.kaarel.homebudgetappv2.dto.TransactionResponse;
import ee.kaarel.homebudgetappv2.dto.TransferRequest;
import ee.kaarel.homebudgetappv2.dto.TransferResponse;
import ee.kaarel.homebudgetappv2.mapper.TransactionMapper;
import ee.kaarel.homebudgetappv2.model.*;
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
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryService categoryService;
    private final UserAccessService userAccessService;
    private final NotificationService notificationService;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAll() {
        Specification<Transaction> spec = TransactionSpecifications.userIdIn(userAccessService.getAccessibleUserIds());
        return transactionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "date"))
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        Transaction transaction = transactionRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Transaction not found"));
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request, Long userId) {
        User targetUser = userAccessService.resolveTargetUser(userId);

        Transaction transaction = new Transaction();
        transaction.setUser(targetUser);
        applyRequestToTransaction(transaction, request, targetUser);

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        Transaction existing = transactionRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Transaction not found"));

        rollbackBalanceImpact(existing);

        applyRequestToTransaction(existing, request, existing.getUser());
        Transaction saved = transactionRepository.save(existing);
        return transactionMapper.toResponse(saved);
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        User currentUser = userAccessService.getCurrentUser();

        TransactionRequest transferRequest = new TransactionRequest();
        transferRequest.setType(TransactionType.TRANSFER);
        transferRequest.setAmount(request.getAmount());
        transferRequest.setDate(request.getDate());
        transferRequest.setDescription(request.getDescription());
        transferRequest.setFromAccountId(request.getFromAccountId());
        transferRequest.setToAccountId(request.getToAccountId());

        Transaction transaction = new Transaction();
        transaction.setUser(currentUser);
        applyRequestToTransaction(transaction, transferRequest, currentUser);

        Transaction saved = transactionRepository.save(transaction);
        Account fromAccount = saved.getFromAccount();
        Account toAccount = saved.getToAccount();

        return new TransferResponse(
                saved.getId(),
                fromAccount.getId(),
                fromAccount.getBalance(),
                toAccount.getId(),
                toAccount.getName()
        );
    }

    @Transactional
    public void delete(Long id) {
        Transaction transaction = transactionRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Transaction not found"));

        rollbackBalanceImpact(transaction);
        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> filter(LocalDate startDate,
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
                .map(transactionMapper::toResponse)
                .toList();
    }

    private void applyRequestToTransaction(Transaction transaction, TransactionRequest request, User owner) {
        validateTypeAccounts(request);

        Category category = resolveCategoryForTransaction(request);
        if (category != null && !category.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Category must belong to transaction owner");
        }

        Account fromAccount = request.getFromAccountId() == null ? null : getAccessibleAccount(request.getFromAccountId());
        Account toAccount = request.getToAccountId() == null
                ? null
                : (request.getType() == TransactionType.TRANSFER
                ? getTransferTargetAccount(request.getToAccountId())
                : getAccessibleAccount(request.getToAccountId()));

        if (fromAccount != null && !fromAccount.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "fromAccount must belong to transaction owner");
        }

        if (request.getType() != TransactionType.TRANSFER && toAccount != null && !toAccount.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "toAccount must belong to transaction owner for non-transfer types");
        }
        if (request.getType() == TransactionType.TRANSFER && !fromAccount.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "You can transfer only from your own account");
        }
        if (request.getType() == TransactionType.TRANSFER && toAccount.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Transfer target must belong to another user");
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

    private Category resolveCategoryForTransaction(TransactionRequest request) {
        if (request.getType() == TransactionType.TRANSFER) {
            return null;
        }

        if (request.getSubCategoryId() != null) {
            Category subCategory = categoryService.getAccessibleCategoryOrThrow(request.getSubCategoryId());
            if (subCategory.getParent() == null) {
                throw new ResponseStatusException(BAD_REQUEST, "subCategoryId must reference a child category");
            }
            if (request.getParentCategoryId() != null
                    && !request.getParentCategoryId().equals(subCategory.getParent().getId())) {
                throw new ResponseStatusException(BAD_REQUEST, "subCategoryId does not belong to parentCategoryId");
            }
            return subCategory;
        }

        if (request.getParentCategoryId() != null) {
            Category parentCategory = categoryService.getAccessibleCategoryOrThrow(request.getParentCategoryId());
            if (parentCategory.getParent() != null) {
                throw new ResponseStatusException(BAD_REQUEST, "parentCategoryId must reference a root category");
            }
            return parentCategory;
        }

        if (request.getCategoryId() != null) {
            return categoryService.getAccessibleCategoryOrThrow(request.getCategoryId());
        }

        throw new ResponseStatusException(BAD_REQUEST, "Category is required for INCOME and EXPENSE");
    }

    private Account getAccessibleAccount(Long accountId) {
        return accountRepository.findByIdAndUserIdIn(accountId, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
    }

    private Account getTransferTargetAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Transfer target account not found"));
    }

    private void validateTypeAccounts(TransactionRequest request) {
        TransactionType type = request.getType();
        switch (type) {
            case INCOME -> {
                if (request.getToAccountId() == null || request.getFromAccountId() != null) {
                    throw new ResponseStatusException(BAD_REQUEST, "INCOME requires only toAccount");
                }
            }
            case EXPENSE -> {
                if (request.getFromAccountId() == null || request.getToAccountId() != null) {
                    throw new ResponseStatusException(BAD_REQUEST, "EXPENSE requires only fromAccount");
                }
            }
            case TRANSFER -> {
                if (request.getFromAccountId() == null || request.getToAccountId() == null) {
                    throw new ResponseStatusException(BAD_REQUEST, "TRANSFER requires both fromAccount and toAccount");
                }
                if (request.getFromAccountId().equals(request.getToAccountId())) {
                    throw new ResponseStatusException(BAD_REQUEST, "Transfer accounts must be different");
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

                if (!transaction.getToAccount().getUser().getId().equals(transaction.getUser().getId())) {
                    String msg = "You received transfer of " + amount + " " + transaction.getToAccount().getCurrency() +
                            " to account '" + transaction.getToAccount().getName() + "'";
                    notificationService.createNotification(transaction.getToAccount().getUser(), msg);
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

    private String resolveSortField(String sortBy) {
        if ("amount".equalsIgnoreCase(sortBy)) {
            return "amount";
        }
        return "date";
    }
}
