package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.MarkRecurringPaymentPaidRequest;
import ee.kaarel.homebudgetappv2.dto.RecurringPaymentNotificationResponse;
import ee.kaarel.homebudgetappv2.dto.RecurringPaymentRequest;
import ee.kaarel.homebudgetappv2.dto.RecurringPaymentResponse;
import ee.kaarel.homebudgetappv2.model.RecurringPayment;
import ee.kaarel.homebudgetappv2.model.RecurringPaymentStatus;
import ee.kaarel.homebudgetappv2.model.SubCategory;
import ee.kaarel.homebudgetappv2.model.Transaction;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.RecurringPaymentRepository;
import ee.kaarel.homebudgetappv2.repository.RecurringPaymentStatusRepository;
import ee.kaarel.homebudgetappv2.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RecurringPaymentService {

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final RecurringPaymentStatusRepository statusRepository;
    private final TransactionRepository transactionRepository;
    private final UserAccessService userAccessService;
    private final CategoryService categoryService;
    private final LocalizationService localizationService;

    @Transactional(readOnly = true)
    public List<RecurringPaymentResponse> getAll() {
        User current = userAccessService.getCurrentUser();
        List<RecurringPayment> payments = userAccessService.isAdmin(current)
                ? recurringPaymentRepository.findAll()
                : userAccessService.isChild(current)
                    ? recurringPaymentRepository.findByOwnerIdOrderByNameAsc(current.getId())
                    : recurringPaymentRepository.findByOwnerFamilyIdOrderByNameAsc(current.getFamilyId());
        LocalDate now = LocalDate.now();
        Map<Long, RecurringPaymentStatus> statuses = loadStatuses(payments, now.getYear(), now.getMonthValue());
        return payments.stream().map(payment -> toResponse(payment, statuses.get(payment.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public List<RecurringPaymentNotificationResponse> getNotifications() {
        LocalDate now = LocalDate.now();
        return getAll().stream()
                .filter(payment -> payment.active() && !payment.paidThisMonth() && payment.dueDay() <= now.getDayOfMonth())
                .map(payment -> new RecurringPaymentNotificationResponse(
                        payment.id(),
                        payment.name(),
                        payment.amount(),
                        payment.dueDay(),
                        payment.subCategoryId(),
                        payment.subCategoryName(),
                        payment.categoryName(),
                        now.getYear(),
                        now.getMonthValue(),
                        payment.paidThisMonth(),
                        payment.paidTransactionId()
                ))
                .toList();
    }

    @Transactional
    public RecurringPaymentResponse create(RecurringPaymentRequest request) {
        User owner = userAccessService.resolveAccountOwner(request.ownerId());
        SubCategory subCategory = categoryService.getAccessibleSubCategory(request.subCategoryId());
        RecurringPayment recurringPayment = new RecurringPayment();
        recurringPayment.setOwner(owner);
        apply(recurringPayment, request, subCategory);
        return toResponse(recurringPaymentRepository.save(recurringPayment), null);
    }

    @Transactional
    public RecurringPaymentResponse update(Long id, RecurringPaymentRequest request) {
        RecurringPayment recurringPayment = getAccessibleRecurringPayment(id);
        User owner = request.ownerId() == null ? recurringPayment.getOwner() : userAccessService.resolveAccountOwner(request.ownerId());
        SubCategory subCategory = categoryService.getAccessibleSubCategory(request.subCategoryId());
        recurringPayment.setOwner(owner);
        apply(recurringPayment, request, subCategory);
        return toResponse(recurringPaymentRepository.save(recurringPayment), getCurrentStatus(recurringPayment));
    }

    @Transactional
    public void delete(Long id) {
        recurringPaymentRepository.delete(getAccessibleRecurringPayment(id));
    }

    @Transactional
    public RecurringPaymentResponse markPaid(Long id, MarkRecurringPaymentPaidRequest request) {
        RecurringPayment recurringPayment = getAccessibleRecurringPayment(id);
        Transaction transaction = transactionRepository.findById(request.transactionId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.transaction.notFound")));
        if (!userAccessService.canAccessTransaction(userAccessService.getCurrentUser(), transaction)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, localizationService.getMessage("error.transaction.accessDenied"));
        }

        LocalDate now = LocalDate.now();
        RecurringPaymentStatus status = statusRepository.findByRecurringPaymentIdAndYearAndMonth(id, now.getYear(), now.getMonthValue())
                .orElseGet(() -> {
                    RecurringPaymentStatus created = new RecurringPaymentStatus();
                    created.setRecurringPayment(recurringPayment);
                    created.setYear(now.getYear());
                    created.setMonth(now.getMonthValue());
                    return created;
                });

        if (status.isPaid()) {
            throw new ResponseStatusException(CONFLICT, localizationService.getMessage("error.recurring.duplicateStatus"));
        }

        status.setPaid(true);
        status.setPaidTransactionId(transaction.getId());
        return toResponse(recurringPayment, statusRepository.save(status));
    }

    private void apply(RecurringPayment recurringPayment, RecurringPaymentRequest request, SubCategory subCategory) {
        recurringPayment.setName(request.name().trim());
        recurringPayment.setAmount(request.amount());
        recurringPayment.setCategory(subCategory);
        recurringPayment.setDueDay(request.dueDay());
        recurringPayment.setActive(request.active() == null || request.active());
    }

    private RecurringPayment getAccessibleRecurringPayment(Long id) {
        User current = userAccessService.getCurrentUser();
        RecurringPayment recurringPayment = userAccessService.isAdmin(current)
                ? recurringPaymentRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.recurring.notFound")))
                : userAccessService.isChild(current)
                    ? recurringPaymentRepository.findByIdAndOwnerId(id, current.getId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.recurring.notFound")))
                    : recurringPaymentRepository.findByIdAndOwnerFamilyId(id, current.getFamilyId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.recurring.notFound")));
        if (!userAccessService.canAccessRecurringPayment(current, recurringPayment)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, localizationService.getMessage("error.family.accessDenied"));
        }
        return recurringPayment;
    }

    private Map<Long, RecurringPaymentStatus> loadStatuses(List<RecurringPayment> payments, int year, int month) {
        List<Long> ids = payments.stream().map(RecurringPayment::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return statusRepository.findByRecurringPaymentIdInAndYearAndMonth(ids, year, month).stream()
                .collect(Collectors.toMap(status -> status.getRecurringPayment().getId(), Function.identity()));
    }

    private RecurringPaymentStatus getCurrentStatus(RecurringPayment recurringPayment) {
        LocalDate now = LocalDate.now();
        return statusRepository.findByRecurringPaymentIdAndYearAndMonth(recurringPayment.getId(), now.getYear(), now.getMonthValue())
                .orElse(null);
    }

    private RecurringPaymentResponse toResponse(RecurringPayment recurringPayment, RecurringPaymentStatus status) {
        return new RecurringPaymentResponse(
                recurringPayment.getId(),
                recurringPayment.getName(),
                recurringPayment.getAmount(),
                recurringPayment.getCategory().getId(),
                recurringPayment.getCategory().getName(),
                recurringPayment.getCategory().getParentCategory().getName(),
                recurringPayment.getDueDay(),
                recurringPayment.getOwner().getId(),
                recurringPayment.getOwner().getUsername(),
                recurringPayment.isActive(),
                status != null && status.isPaid(),
                status == null ? null : status.getPaidTransactionId()
        );
    }
}
