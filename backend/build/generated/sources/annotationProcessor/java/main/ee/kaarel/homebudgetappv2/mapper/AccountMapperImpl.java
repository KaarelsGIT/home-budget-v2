package ee.kaarel.homebudgetappv2.mapper;

import ee.kaarel.homebudgetappv2.dto.AccountResponse;
import ee.kaarel.homebudgetappv2.model.Account;
import ee.kaarel.homebudgetappv2.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-19T14:43:08+0200",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.1.jar, environment: Java 21.0.7 (Homebrew)"
)
@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public AccountResponse toResponse(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountResponse accountResponse = new AccountResponse();

        accountResponse.setUserId( accountUserId( account ) );
        accountResponse.setId( account.getId() );
        accountResponse.setName( account.getName() );
        accountResponse.setBalance( account.getBalance() );
        accountResponse.setCurrency( account.getCurrency() );

        return accountResponse;
    }

    private Long accountUserId(Account account) {
        User user = account.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }
}
