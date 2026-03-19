package ee.kaarel.homebudgetappv2.mapper;

import ee.kaarel.homebudgetappv2.dto.TransactionResponse;
import ee.kaarel.homebudgetappv2.model.Account;
import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.Transaction;
import ee.kaarel.homebudgetappv2.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-19T18:14:04+0200",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.1.jar, environment: Java 21.0.7 (Homebrew)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionResponse toResponse(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        TransactionResponse transactionResponse = new TransactionResponse();

        transactionResponse.setUserId( transactionUserId( transaction ) );
        transactionResponse.setCategoryId( transactionCategoryId( transaction ) );
        transactionResponse.setFromAccountId( transactionFromAccountId( transaction ) );
        transactionResponse.setToAccountId( transactionToAccountId( transaction ) );
        transactionResponse.setId( transaction.getId() );
        transactionResponse.setType( transaction.getType() );
        transactionResponse.setAmount( transaction.getAmount() );
        transactionResponse.setDate( transaction.getDate() );
        transactionResponse.setDescription( transaction.getDescription() );
        transactionResponse.setCreatedAt( transaction.getCreatedAt() );
        transactionResponse.setUpdatedAt( transaction.getUpdatedAt() );

        transactionResponse.setCategoryName( transaction.getCategory() != null ? transaction.getCategory().getName() : null );
        transactionResponse.setParentCategoryId( transaction.getCategory() != null && transaction.getCategory().getParent() != null ? transaction.getCategory().getParent().getId() : null );
        transactionResponse.setParentCategoryName( transaction.getCategory() != null && transaction.getCategory().getParent() != null ? transaction.getCategory().getParent().getName() : null );
        transactionResponse.setSubCategoryId( transaction.getCategory() != null && transaction.getCategory().getParent() != null ? transaction.getCategory().getId() : null );
        transactionResponse.setSubCategoryName( transaction.getCategory() != null && transaction.getCategory().getParent() != null ? transaction.getCategory().getName() : null );

        return transactionResponse;
    }

    private Long transactionUserId(Transaction transaction) {
        User user = transaction.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }

    private Long transactionCategoryId(Transaction transaction) {
        Category category = transaction.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getId();
    }

    private Long transactionFromAccountId(Transaction transaction) {
        Account fromAccount = transaction.getFromAccount();
        if ( fromAccount == null ) {
            return null;
        }
        return fromAccount.getId();
    }

    private Long transactionToAccountId(Transaction transaction) {
        Account toAccount = transaction.getToAccount();
        if ( toAccount == null ) {
            return null;
        }
        return toAccount.getId();
    }
}
