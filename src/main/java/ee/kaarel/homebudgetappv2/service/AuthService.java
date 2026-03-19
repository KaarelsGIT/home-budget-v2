package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.AuthResponse;
import ee.kaarel.homebudgetappv2.dto.LoginRequest;
import ee.kaarel.homebudgetappv2.dto.RegisterRequest;
import ee.kaarel.homebudgetappv2.model.Role;
import ee.kaarel.homebudgetappv2.model.User;
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

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(BAD_REQUEST, "Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        if (request.getRole() == Role.CHILD) {
            if (request.getParentId() == null) {
                throw new ResponseStatusException(BAD_REQUEST, "parentId is required for child user");
            }

            User parent = userRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Parent not found"));

            if (parent.getRole() != Role.PARENT) {
                throw new ResponseStatusException(BAD_REQUEST, "Provided parentId does not belong to a parent");
            }

            user.setParent(parent);
        }

        User saved = userRepository.save(user);
        AuthUserDetails authUserDetails = new AuthUserDetails(saved.getId(), saved.getEmail(), saved.getPassword(), saved.getRole());
        String token = jwtService.generateToken(authUserDetails);
        return new AuthResponse(token, saved.getId(), saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Invalid credentials"));

        AuthUserDetails authUserDetails = new AuthUserDetails(user.getId(), user.getEmail(), user.getPassword(), user.getRole());
        String token = jwtService.generateToken(authUserDetails);

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole());
    }
}
