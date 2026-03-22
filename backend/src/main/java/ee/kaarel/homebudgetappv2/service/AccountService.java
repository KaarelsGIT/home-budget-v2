package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.AccountMemberRequest;
import ee.kaarel.homebudgetappv2.dto.AccountMemberResponse;
import ee.kaarel.homebudgetappv2.dto.AccountRequest;
import ee.kaarel.homebudgetappv2.dto.AccountResponse;
import ee.kaarel.homebudgetappv2.model.Account;
import ee.kaarel.homebudgetappv2.model.AccountMember;
import ee.kaarel.homebudgetappv2.model.AccountMemberRole;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.AccountMemberRepository;
import ee.kaarel.homebudgetappv2.repository.AccountRepository;
import ee.kaarel.homebudgetappv2.repository.TransactionRepository;
import ee.kaarel.homebudgetappv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserAccessService userAccessService;
    private final LocalizationService localizationService;

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccounts(Long ownerId) {
        User current = userAccessService.getCurrentUser();
        List<Account> accounts;
        if (userAccessService.isAdmin(current)) {
            accounts = ownerId == null
                    ? accountRepository.findAll()
                    : accountRepository.findByOwnerIdOrderByNameAsc(ownerId);
        } else if (userAccessService.isChild(current)) {
            accounts = accountRepository.findByOwnerIdOrderByNameAsc(current.getId());
        } else if (ownerId != null) {
            User owner = userAccessService.getAccessibleUserOrThrow(ownerId);
            accounts = accountRepository.findByOwnerIdOrderByNameAsc(owner.getId());
        } else {
            accounts = accountRepository.findByOwnerFamilyIdOrderByNameAsc(current.getFamilyId());
        }
        return accounts.stream()
                .filter(account -> userAccessService.canAccessAccount(current, account))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        return toResponse(getAccessibleAccountOrThrow(id));
    }

    @Transactional
    public AccountResponse create(AccountRequest request) {
        User owner = userAccessService.resolveAccountOwner(request.ownerId());
        Account account = new Account();
        apply(account, request, owner);
        Account saved = accountRepository.save(account);
        syncMembers(saved, request.members(), owner);
        return toResponse(saved);
    }

    @Transactional
    public AccountResponse update(Long id, AccountRequest request) {
        Account account = getAccessibleAccountOrThrow(id);
        User owner = request.ownerId() == null ? account.getOwner() : userAccessService.resolveAccountOwner(request.ownerId());
        apply(account, request, owner);
        Account saved = accountRepository.save(account);
        syncMembers(saved, request.members(), owner);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Account account = getAccessibleAccountOrThrow(id);
        accountMemberRepository.deleteByAccountId(account.getId());
        accountRepository.delete(account);
    }

    @Transactional
    public Account createDefaultAccountFor(User user) {
        Account account = new Account();
        account.setName("Default");
        account.setOwner(user);
        account.setDefault(true);
        Account saved = accountRepository.save(account);
        AccountMember member = new AccountMember();
        member.setAccount(saved);
        member.setUser(user);
        member.setRole(AccountMemberRole.OWNER);
        accountMemberRepository.save(member);
        return saved;
    }

    @Transactional(readOnly = true)
    public Account getAccessibleAccountOrThrow(Long accountId) {
        User current = userAccessService.getCurrentUser();
        Account account = userAccessService.isAdmin(current)
                ? accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.account.notFound")))
                : userAccessService.isChild(current)
                    ? accountRepository.findByIdAndOwnerId(accountId, current.getId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.account.notFound")))
                    : accountRepository.findByIdAndOwnerFamilyId(accountId, current.getFamilyId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.account.notFound")));
        if (!userAccessService.canAccessAccount(current, account)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, localizationService.getMessage("error.account.accessDenied"));
        }
        return account;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateBalance(Long accountId) {
        BigDecimal balance = transactionRepository.calculateBalance(accountId);
        return balance == null ? BigDecimal.ZERO : balance;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateBalanceExcluding(Long accountId, Long transactionId) {
        BigDecimal balance = transactionRepository.calculateBalanceExcluding(accountId, transactionId);
        return balance == null ? BigDecimal.ZERO : balance;
    }

    private void apply(Account account, AccountRequest request, User owner) {
        account.setName(request.name().trim());
        account.setOwner(owner);
        account.setDefault(Boolean.TRUE.equals(request.isDefault()));
    }

    private void syncMembers(Account account, List<AccountMemberRequest> memberRequests, User owner) {
        accountMemberRepository.deleteByAccountId(account.getId());
        List<AccountMemberRequest> requests = memberRequests == null ? List.of() : memberRequests;
        Map<Long, AccountMemberRole> roles = new LinkedHashMap<>();
        roles.put(owner.getId(), AccountMemberRole.OWNER);
        for (AccountMemberRequest request : requests) {
            User memberUser = userRepository.findById(request.userId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.user.notFound")));
            if (!owner.getFamilyId().equals(memberUser.getFamilyId())) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, localizationService.getMessage("error.family.accessDenied"));
            }
            roles.put(memberUser.getId(), request.role());
        }

        List<AccountMember> members = new ArrayList<>();
        roles.forEach((userId, role) -> {
            User memberUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.user.notFound")));
            AccountMember member = new AccountMember();
            member.setAccount(account);
            member.setUser(memberUser);
            member.setRole(role);
            members.add(member);
        });
        accountMemberRepository.saveAll(members);
    }

    private AccountResponse toResponse(Account account) {
        List<AccountMemberResponse> members = accountMemberRepository.findByAccountId(account.getId()).stream()
                .map(member -> new AccountMemberResponse(
                        member.getUser().getId(),
                        member.getUser().getUsername(),
                        member.getRole()
                ))
                .toList();

        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getOwner().getId(),
                account.getOwner().getUsername(),
                account.isDefault(),
                calculateBalance(account.getId()),
                members
        );
    }
}
