package ee.kaarel.homebudgetappv2.mapper;

import ee.kaarel.homebudgetappv2.dto.RecurringTransactionResponse;
import ee.kaarel.homebudgetappv2.model.RecurringTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecurringTransactionMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "userId", source = "user.id")
    RecurringTransactionResponse toResponse(RecurringTransaction recurringTransaction);
}
