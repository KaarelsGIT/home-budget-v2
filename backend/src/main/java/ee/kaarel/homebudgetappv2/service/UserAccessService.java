package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.model.Role;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.UserRepository;
import ee.kaarel.homebudgetappv2.security.AuthUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserAccessService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserDetails principal)) {
            throw new ResponseStatusException(FORBIDDEN, "Unauthenticated");
        }

        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }

    public List<Long> getAccessibleUserIds() {
        return getAccessibleUsers().stream().map(User::getId).toList();
    }

    public List<User> getAccessibleUsers() {
        User current = getCurrentUser();
        List<User> users = new ArrayList<>();
        users.add(current);

        if (current.getRole() == Role.PARENT) {
            users.addAll(userRepository.findByParentId(current.getId()));
        }

        return users;
    }

    public User resolveTargetUser(Long requestedUserId) {
        User current = getCurrentUser();
        if (requestedUserId == null || requestedUserId.equals(current.getId())) {
            return current;
        }

        if (current.getRole() != Role.PARENT) {
            throw new ResponseStatusException(FORBIDDEN, "Child cannot access other users");
        }

        User target = userRepository.findById(requestedUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Target user not found"));

        boolean isChild = target.getParent() != null && target.getParent().getId().equals(current.getId());
        if (!isChild) {
            throw new ResponseStatusException(FORBIDDEN, "Parent can access only own children data");
        }

        return target;
    }

    public void assertCanAccessUser(Long userId) {
        if (!getAccessibleUserIds().contains(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }
}
