package ee.kaarel.homebudgetappv2.controller;

import ee.kaarel.homebudgetappv2.dto.CreateTransactionRequest;
import ee.kaarel.homebudgetappv2.dto.TransactionDTO;
import ee.kaarel.homebudgetappv2.dto.TransactionFilterRequest;
import ee.kaarel.homebudgetappv2.dto.TransferRequest;
import ee.kaarel.homebudgetappv2.model.TransactionType;
import ee.kaarel.homebudgetappv2.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(transactionService.getAll(
                new TransactionFilterRequest(startDate, endDate, subCategoryId, type, accountId, userId)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.createTransaction(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, @Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> createTransfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.createTransaction(new CreateTransactionRequest(
                TransactionType.TRANSFER,
                request.amount(),
                request.fromAccountId(),
                request.toAccountId(),
                request.subCategoryId()
        )));
    }
}
