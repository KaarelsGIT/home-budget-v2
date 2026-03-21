package ee.kaarel.homebudgetappv2.controller;

import ee.kaarel.homebudgetappv2.dto.UserSummaryDto;
import ee.kaarel.homebudgetappv2.service.UserService;
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

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserSummaryDto>> getUsers() {
        return ResponseEntity.ok(userService.getAll());
    }
}
