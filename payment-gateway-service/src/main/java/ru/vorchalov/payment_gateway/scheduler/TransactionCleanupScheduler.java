package ru.vorchalov.payment_gateway.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

@Slf4j
@Component
public class TransactionCleanupScheduler {

    private final PaymentTransactionService transactionService;

    public TransactionCleanupScheduler(PaymentTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Scheduled(fixedDelayString = "60000")
    public void autoDeclineExpiredTransactions() {
        log.info("⏳ Проверка просроченных транзакций...");
        transactionService.autoDeclineExpiredTransactions();
    }
}
