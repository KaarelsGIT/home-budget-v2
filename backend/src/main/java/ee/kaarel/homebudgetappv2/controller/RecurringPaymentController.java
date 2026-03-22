package ee.kaarel.homebudgetappv2.controller;

import ee.kaarel.homebudgetappv2.dto.MarkRecurringPaymentPaidRequest;
import ee.kaarel.homebudgetappv2.dto.RecurringPaymentNotificationResponse;
import ee.kaarel.homebudgetappv2.dto.RecurringPaymentRequest;
import ee.kaarel.homebudgetappv2.dto.RecurringPaymentResponse;
import ee.kaarel.homebudgetappv2.service.RecurringPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recurring-payments")
@RequiredArgsConstructor
public class RecurringPaymentController {

    private final RecurringPaymentService recurringPaymentService;

    @GetMapping
    public ResponseEntity<List<RecurringPaymentResponse>> getRecurringPayments() {
        return ResponseEntity.ok(recurringPaymentService.getAll());
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<RecurringPaymentNotificationResponse>> getNotifications() {
        return ResponseEntity.ok(recurringPaymentService.getNotifications());
    }

    @PostMapping
    public ResponseEntity<RecurringPaymentResponse> createRecurringPayment(@Valid @RequestBody RecurringPaymentRequest request) {
        return ResponseEntity.ok(recurringPaymentService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringPaymentResponse> updateRecurringPayment(
            @PathVariable Long id,
            @Valid @RequestBody RecurringPaymentRequest request
    ) {
        return ResponseEntity.ok(recurringPaymentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringPayment(@PathVariable Long id) {
        recurringPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<RecurringPaymentResponse> markPaid(
            @PathVariable Long id,
            @Valid @RequestBody MarkRecurringPaymentPaidRequest request
    ) {
        return ResponseEntity.ok(recurringPaymentService.markPaid(id, request));
    }
}
