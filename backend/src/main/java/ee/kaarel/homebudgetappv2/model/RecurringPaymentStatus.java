package ee.kaarel.homebudgetappv2.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recurring_payment_statuses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"recurring_payment_id", "year", "month"}))
@Getter
@Setter
public class RecurringPaymentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_payment_id", nullable = false)
    private RecurringPayment recurringPayment;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private boolean isPaid;

    @Column
    private Long paidTransactionId;
}
