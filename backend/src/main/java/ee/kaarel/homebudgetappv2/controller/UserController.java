package ee.kaarel.homebudgetappv2.controller;

import ee.kaarel.homebudgetappv2.dto.UserDTO;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.service.UserAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/users", "/api/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserAccessService userAccessService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> users = userAccessService.getAccessibleUsers().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    private UserDTO toDto(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getParent() != null ? user.getParent().getId() : null
        );
    }
}
