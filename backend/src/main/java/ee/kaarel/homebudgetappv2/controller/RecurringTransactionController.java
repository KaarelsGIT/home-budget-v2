package ee.kaarel.homebudgetappv2.controller;

import ee.kaarel.homebudgetappv2.dto.RecurringTransactionRequest;
import ee.kaarel.homebudgetappv2.dto.RecurringTransactionResponse;
import ee.kaarel.homebudgetappv2.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recurring")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> getAll() {
        return ResponseEntity.ok(recurringTransactionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recurringTransactionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> create(@Valid @RequestBody RecurringTransactionRequest request,
                                                               @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(recurringTransactionService.create(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> update(@PathVariable Long id,
                                                               @Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.ok(recurringTransactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recurringTransactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
