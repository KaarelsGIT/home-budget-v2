package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.UpdateUserFamilyRequest;
import ee.kaarel.homebudgetappv2.dto.UserDTO;
import ee.kaarel.homebudgetappv2.dto.UserSummaryDto;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.model.UserStatus;
import ee.kaarel.homebudgetappv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAccessService userAccessService;
    private final LocalizationService localizationService;

    @Transactional(readOnly = true)
    public List<UserSummaryDto> getUsers() {
        return userAccessService.getScopedUsers().stream()
                .map(user -> new UserSummaryDto(user.getId(), user.getUsername(), user.getRole(), user.getStatus()))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDTO getUser(Long id) {
        return toDto(userAccessService.getAccessibleUserOrThrow(id));
    }

    @Transactional
    public UserDTO approveUser(Long id) {
        userAccessService.assertAdmin();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.user.notFound")));
        user.setStatus(UserStatus.APPROVED);
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDTO updateFamily(Long id, UpdateUserFamilyRequest request) {
        userAccessService.assertAdmin();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.user.notFound")));
        user.setFamilyId(request.familyId());
        return toDto(userRepository.save(user));
    }

    private UserDTO toDto(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getParent() == null ? null : user.getParent().getId(),
                user.getFamilyId()
        );
    }
}
