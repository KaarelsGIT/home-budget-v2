package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.UserSummaryDto;
import ee.kaarel.homebudgetappv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserSummaryDto> getAll() {
        return userRepository.findAll().stream()
                .map(user -> new UserSummaryDto(user.getId(), user.getEmail(), user.getEmail()))
                .toList();
    }
}
