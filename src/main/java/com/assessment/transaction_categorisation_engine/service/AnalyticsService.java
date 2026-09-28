package com.assessment.transaction_categorisation_engine.service;

import com.assessment.transaction_categorisation_engine.dto.AnalyticsSummaryResponse;
import com.assessment.transaction_categorisation_engine.dto.CategorySpend;
import com.assessment.transaction_categorisation_engine.dto.MerchantSpend;
import com.assessment.transaction_categorisation_engine.dto.MonthOverMonthDelta;
import com.assessment.transaction_categorisation_engine.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public AnalyticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse summary() {
        List<CategorySpend> byCategory = transactionRepository.sumAmountByCategory().stream()
                .map(row -> new CategorySpend(
                        String.valueOf(row[0]),
                        toAmount(row[1])
                ))
                .toList();

        List<MerchantSpend> topMerchants = transactionRepository
                .topMerchantsBySpend(PageRequest.of(0, 10))
                .stream()
                .map(row -> new MerchantSpend(
                        String.valueOf(row[0]),
                        toAmount(row[1])
                ))
                .toList();

        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);

        BigDecimal currentSpend = defaultZero(transactionRepository.sumAmountBetween(
                current.atDay(1), current.atEndOfMonth()));
        BigDecimal previousSpend = defaultZero(transactionRepository.sumAmountBetween(
                previous.atDay(1), previous.atEndOfMonth()));
        BigDecimal absoluteDelta = currentSpend.subtract(previousSpend);
        BigDecimal percentDelta = previousSpend.compareTo(BigDecimal.ZERO) == 0
                ? null
                : absoluteDelta.multiply(BigDecimal.valueOf(100))
                .divide(previousSpend, 2, RoundingMode.HALF_UP);

        MonthOverMonthDelta mom = new MonthOverMonthDelta(
                current.toString(),
                currentSpend,
                previous.toString(),
                previousSpend,
                absoluteDelta,
                percentDelta
        );

        return new AnalyticsSummaryResponse(byCategory, topMerchants, mom);
    }

    private BigDecimal toAmount(Object value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
