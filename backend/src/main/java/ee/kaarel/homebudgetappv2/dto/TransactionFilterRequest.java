package ee.kaarel.homebudgetappv2.dto;

import ee.kaarel.homebudgetappv2.model.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TransactionFilterRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Long subCategoryId,
        TransactionType type,
        Long accountId,
        Long userId
) {
}
