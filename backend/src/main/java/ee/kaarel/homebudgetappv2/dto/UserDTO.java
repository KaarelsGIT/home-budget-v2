package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private Role role;
    private Long parentId;
}
