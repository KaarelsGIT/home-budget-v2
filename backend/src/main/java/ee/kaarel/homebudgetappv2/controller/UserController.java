package ee.kaarel.homebudgetappv2.controller;

import ee.kaarel.homebudgetappv2.dto.UpdateUserFamilyRequest;
import ee.kaarel.homebudgetappv2.dto.UserDTO;
import ee.kaarel.homebudgetappv2.dto.UserSummaryDto;
import ee.kaarel.homebudgetappv2.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserSummaryDto>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<UserDTO> approveUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.approveUser(id));
    }

    @PatchMapping("/{id}/family")
    public ResponseEntity<UserDTO> updateFamily(@PathVariable Long id, @Valid @RequestBody UpdateUserFamilyRequest request) {
        return ResponseEntity.ok(userService.updateFamily(id, request));
    }
}
