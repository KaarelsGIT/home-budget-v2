package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private Long parentCategoryId;
    private String parentCategoryName;
    private Long subCategoryId;
    private String subCategoryName;
    private Long fromAccountId;
    private Long toAccountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
