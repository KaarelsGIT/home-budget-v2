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

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserAccessService userAccessService;

    @Transactional(readOnly = true)
    public List<AccountResponse> getAll() {
        return accountRepository.findByUserIdIn(userAccessService.getAccessibleUserIds())
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
        account.setName(request.getName());
        account.setBalance(request.getBalance());
        account.setCurrency(request.getCurrency().toUpperCase());
        account.setUser(targetUser);

        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse update(Long id, AccountRequest request) {
        Account account = accountRepository.findByIdAndUserIdIn(id, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));

        account.setName(request.getName());
        account.setBalance(request.getBalance());
        account.setCurrency(request.getCurrency().toUpperCase());

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
        return accountRepository.findByIdAndUserIdIn(accountId, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
    }
}
