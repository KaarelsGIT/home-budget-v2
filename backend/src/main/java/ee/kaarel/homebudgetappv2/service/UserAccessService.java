package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.model.Account;
import ee.kaarel.homebudgetappv2.model.RecurringPayment;
import ee.kaarel.homebudgetappv2.model.Role;
import ee.kaarel.homebudgetappv2.model.Transaction;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.UserRepository;
import ee.kaarel.homebudgetappv2.security.AuthUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserAccessService {

    private final UserRepository userRepository;
    private final LocalizationService localizationService;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserDetails principal)) {
            throw new ResponseStatusException(FORBIDDEN, localizationService.getMessage("error.unauthenticated"));
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.user.notFound")));
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public boolean isParent(User user) {
        return user.getRole() == Role.PARENT;
    }

    public boolean isChild(User user) {
        return user.getRole() == Role.CHILD;
    }

    public void assertAdmin() {
        if (!isAdmin(getCurrentUser())) {
            throw new ResponseStatusException(FORBIDDEN, localizationService.getMessage("error.admin.required"));
        }
    }

    public User getAccessibleUserOrThrow(Long userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.user.notFound")));
        if (!canAccessUser(getCurrentUser(), target)) {
            throw new ResponseStatusException(FORBIDDEN, localizationService.getMessage("error.user.accessDenied"));
        }
        return target;
    }

    public User resolveAccountOwner(Long ownerId) {
        User current = getCurrentUser();
        if (ownerId == null) {
            return current;
        }
        User target = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.user.notFound")));
        if (isAdmin(current)) {
            return target;
        }
        if (isChild(current)) {
            if (!current.getId().equals(ownerId)) {
                throw new ResponseStatusException(FORBIDDEN, localizationService.getMessage("error.user.accessDenied"));
            }
            return current;
        }
        if (!current.getFamilyId().equals(target.getFamilyId())) {
            throw new ResponseStatusException(FORBIDDEN, localizationService.getMessage("error.family.accessDenied"));
        }
        return target;
    }

    public List<User> getScopedUsers() {
        User current = getCurrentUser();
        if (isAdmin(current)) {
            return userRepository.findAll();
        }
        if (isChild(current)) {
            return List.of(current);
        }
        return userRepository.findByFamilyIdOrderByUsernameAsc(current.getFamilyId());
    }

    public boolean canAccessUser(User actor, User target) {
        if (isAdmin(actor)) {
            return true;
        }
        if (isChild(actor)) {
            return actor.getId().equals(target.getId());
        }
        return actor.getFamilyId().equals(target.getFamilyId());
    }

    public boolean canAccessAccount(User actor, Account account) {
        if (isAdmin(actor)) {
            return true;
        }
        if (isChild(actor)) {
            return account.getOwner().getId().equals(actor.getId());
        }
        return account.getOwner().getFamilyId().equals(actor.getFamilyId());
    }

    public boolean canAccessTransaction(User actor, Transaction transaction) {
        Account from = transaction.getFromAccount();
        Account to = transaction.getToAccount();
        if (isAdmin(actor)) {
            return true;
        }
        if (isChild(actor)) {
            return (from != null && from.getOwner().getId().equals(actor.getId()))
                    || (to != null && to.getOwner().getId().equals(actor.getId()))
                    || transaction.getCreatedBy().getId().equals(actor.getId());
        }
        return (from != null && from.getOwner().getFamilyId().equals(actor.getFamilyId()))
                || (to != null && to.getOwner().getFamilyId().equals(actor.getFamilyId()));
    }

    public boolean canAccessRecurringPayment(User actor, RecurringPayment recurringPayment) {
        if (isAdmin(actor)) {
            return true;
        }
        if (isChild(actor)) {
            return recurringPayment.getOwner().getId().equals(actor.getId());
        }
        return recurringPayment.getOwner().getFamilyId().equals(actor.getFamilyId());
    }
}
