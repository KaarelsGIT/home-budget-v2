package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.model.Role;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.model.UserStatus;
import ee.kaarel.homebudgetappv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class AdminBootstrapService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    @Bean
    ApplicationRunner ensureAdminUser() {
        return args -> {
            if (userRepository.existsByRole(Role.ADMIN)) {
                return;
            }
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.APPROVED);
            admin.setFamilyId(UUID.randomUUID());
            User saved = userRepository.save(admin);
            accountService.createDefaultAccountFor(saved);
        };
    }
}
