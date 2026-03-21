package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.AccountRequest;
import ee.kaarel.homebudgetappv2.dto.AccountResponse;
import ee.kaarel.homebudgetappv2.mapper.AccountMapper;
import ee.kaarel.homebudgetappv2.model.Account;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserAccessService userAccessService;

    @Transactional(readOnly = true)
    public List<AccountResponse> getAll(Long userId) {
        List<Account> accounts;

        if (userId != null) {
            userAccessService.getUserById(userId);
            accounts = accountRepository.findByUserIdOrderByNameAsc(userId);
        } else {
            accounts = accountRepository.findAllByOrderByUserIdAscNameAsc();
        }

        return accounts
                .stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        Account account = accountRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
        return accountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse create(AccountRequest request, Long userId) {
        User targetUser = userAccessService.resolveTargetUser(userId);

        Account account = new Account();
        applyRequest(account, request, targetUser);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse update(Long id, AccountRequest request) {
        Account account = accountRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));

        applyRequest(account, request, account.getUser());
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public void delete(Long id) {
        Account account = accountRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
        accountRepository.delete(account);
    }

    @Transactional(readOnly = true)
    public Account getAccessibleAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
    }

    @Transactional(readOnly = true)
    public Account getOwnedAccountOrThrow(Long accountId) {
        Account account = getAccessibleAccountOrThrow(accountId);
        if (!account.getUser().getId().equals(userAccessService.getCurrentUser().getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Source account must belong to the current user");
        }
        return account;
    }

    private void applyRequest(Account account, AccountRequest request, User owner) {
        account.setName(request.getName().trim());
        account.setBalance(request.getBalance());
        account.setType(request.getType());
        account.setUser(owner);

        if (request.getParentAccountId() == null) {
            account.setParentAccount(null);
            return;
        }

        if (account.getId() != null && account.getId().equals(request.getParentAccountId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Account cannot be its own parent");
        }

        Account parentAccount = accountRepository.findById(request.getParentAccountId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Parent account not found"));
        if (!parentAccount.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Parent account must belong to the same user");
        }

        account.setParentAccount(parentAccount);
    }
}
