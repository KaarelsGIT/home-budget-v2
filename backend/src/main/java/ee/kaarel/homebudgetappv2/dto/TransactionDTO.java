package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDTO {

    private Long id;

    @NotNull
    private TransactionType type;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate date;

    private String description;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private Long fromAccountId;
    private Long toAccountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
