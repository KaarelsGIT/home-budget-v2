package ee.kaarel.homebudgetappv2.mapper;

import ee.kaarel.homebudgetappv2.dto.AccountResponse;
import ee.kaarel.homebudgetappv2.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "parentAccountId", source = "parentAccount.id")
    AccountResponse toResponse(Account account);
}
