package ee.kaarel.homebudgetappv2.mapper;

import ee.kaarel.homebudgetappv2.dto.RecurringTransactionResponse;
import ee.kaarel.homebudgetappv2.model.Account;
import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.RecurringTransaction;
import ee.kaarel.homebudgetappv2.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-19T18:14:04+0200",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.1.jar, environment: Java 21.0.7 (Homebrew)"
)
@Component
public class RecurringTransactionMapperImpl implements RecurringTransactionMapper {

    @Override
    public RecurringTransactionResponse toResponse(RecurringTransaction recurringTransaction) {
        if ( recurringTransaction == null ) {
            return null;
        }

        RecurringTransactionResponse recurringTransactionResponse = new RecurringTransactionResponse();

        recurringTransactionResponse.setCategoryId( recurringTransactionCategoryId( recurringTransaction ) );
        recurringTransactionResponse.setAccountId( recurringTransactionAccountId( recurringTransaction ) );
        recurringTransactionResponse.setUserId( recurringTransactionUserId( recurringTransaction ) );
        recurringTransactionResponse.setId( recurringTransaction.getId() );
        recurringTransactionResponse.setAmount( recurringTransaction.getAmount() );
        recurringTransactionResponse.setFrequency( recurringTransaction.getFrequency() );
        recurringTransactionResponse.setNextExecutionDate( recurringTransaction.getNextExecutionDate() );
        recurringTransactionResponse.setActive( recurringTransaction.isActive() );

        return recurringTransactionResponse;
    }

    private Long recurringTransactionCategoryId(RecurringTransaction recurringTransaction) {
        Category category = recurringTransaction.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getId();
    }

    private Long recurringTransactionAccountId(RecurringTransaction recurringTransaction) {
        Account account = recurringTransaction.getAccount();
        if ( account == null ) {
            return null;
        }
        return account.getId();
    }

    private Long recurringTransactionUserId(RecurringTransaction recurringTransaction) {
        User user = recurringTransaction.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }
}
