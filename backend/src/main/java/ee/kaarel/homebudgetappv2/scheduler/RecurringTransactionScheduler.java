package ee.kaarel.homebudgetappv2.scheduler;

import ee.kaarel.homebudgetappv2.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionScheduler {

    private final RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "0 0 2 * * *")
    public void runDailyRecurringTransactions() {
        log.info("Running daily recurring transaction scheduler");
        recurringTransactionService.executeDueRecurringTransactions();
    }
}
