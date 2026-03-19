package ee.kaarel.homebudgetappv2.mapper;

import ee.kaarel.homebudgetappv2.dto.TransactionResponse;
import ee.kaarel.homebudgetappv2.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", expression = "java(transaction.getCategory() != null ? transaction.getCategory().getName() : null)")
    @Mapping(target = "parentCategoryId", expression = "java(transaction.getCategory() != null && transaction.getCategory().getParent() != null ? transaction.getCategory().getParent().getId() : null)")
    @Mapping(target = "parentCategoryName", expression = "java(transaction.getCategory() != null && transaction.getCategory().getParent() != null ? transaction.getCategory().getParent().getName() : null)")
    @Mapping(target = "subCategoryId", expression = "java(transaction.getCategory() != null && transaction.getCategory().getParent() != null ? transaction.getCategory().getId() : null)")
    @Mapping(target = "subCategoryName", expression = "java(transaction.getCategory() != null && transaction.getCategory().getParent() != null ? transaction.getCategory().getName() : null)")
    @Mapping(target = "fromAccountId", source = "fromAccount.id")
    @Mapping(target = "toAccountId", source = "toAccount.id")
    TransactionResponse toResponse(Transaction transaction);
}
