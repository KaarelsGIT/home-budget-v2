package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.AuthResponse;
import ee.kaarel.homebudgetappv2.dto.LoginRequest;
import ee.kaarel.homebudgetappv2.dto.RegisterRequest;
import ee.kaarel.homebudgetappv2.dto.UserDTO;
import ee.kaarel.homebudgetappv2.model.Role;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.model.UserStatus;
import ee.kaarel.homebudgetappv2.repository.UserRepository;
import ee.kaarel.homebudgetappv2.security.AuthUserDetails;
import ee.kaarel.homebudgetappv2.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountService accountService;
    private final LocalizationService localizationService;

    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username().trim().toLowerCase())) {
            throw new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.user.usernameTaken"));
        }

        User user = new User();
        user.setUsername(request.username().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setStatus(UserStatus.PENDING);

        if (request.role() == Role.CHILD) {
            if (request.parentId() == null) {
                throw new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.user.childNeedsParent"));
            }
            User parent = userRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.user.parentNotFound")));
            if (parent.getRole() != Role.PARENT) {
                throw new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.user.parentRequired"));
            }
            user.setParent(parent);
            user.setFamilyId(parent.getFamilyId());
        } else if (request.role() == Role.PARENT) {
            user.setFamilyId(UUID.randomUUID());
        } else {
            throw new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.user.adminRegisterBlocked"));
        }

        User saved = userRepository.save(user);
        accountService.createDefaultAccountFor(saved);
        return new UserDTO(
                saved.getId(),
                saved.getUsername(),
                saved.getRole(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getParent() == null ? null : saved.getParent().getId(),
                saved.getFamilyId()
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username().trim().toLowerCase(), request.password())
        );

        User user = userRepository.findByUsername(request.username().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, localizationService.getMessage("error.auth.invalidCredentials")));
        AuthUserDetails authUserDetails = new AuthUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.getStatus() == UserStatus.APPROVED
        );
        String token = jwtService.generateToken(authUserDetails);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole(), user.getStatus(), user.getFamilyId());
    }
}
