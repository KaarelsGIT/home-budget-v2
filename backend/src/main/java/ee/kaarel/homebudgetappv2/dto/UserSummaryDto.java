package ee.kaarel.homebudgetappv2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String username;
    private String email;
}
