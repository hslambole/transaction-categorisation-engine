package com.assessment.transaction_categorisation_engine.repository;

import com.assessment.transaction_categorisation_engine.domain.Source;
import com.assessment.transaction_categorisation_engine.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByMerchantNameAndAmountAndCurrencyAndTransactionDateAndSource(
            String merchantName,
            BigDecimal amount,
            String currency,
            LocalDate transactionDate,
            Source source
    );

    @Query("""
            select distinct t.merchantName
            from Transaction t
            where t.merchantName not in (
                select mc.merchantName from MerchantCategory mc
            )
            """)
    List<String> findUncategorisedMerchantNames();

    @Query("""
            select t
            from Transaction t
            left join MerchantCategory mc on mc.merchantName = t.merchantName
            where (:source is null or t.source = :source)
              and (:fromDate is null or t.transactionDate >= :fromDate)
              and (:toDate is null or t.transactionDate <= :toDate)
              and (:category is null or mc.category = :category)
            """)
    Page<Transaction> search(
            @Param("category") String category,
            @Param("source") Source source,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query("""
            select coalesce(mc.category, 'UNCATEGORISED') as category, sum(t.amount) as total
            from Transaction t
            left join MerchantCategory mc on mc.merchantName = t.merchantName
            group by coalesce(mc.category, 'UNCATEGORISED')
            """)
    List<Object[]> sumAmountByCategory();

    @Query("""
            select t.merchantName, sum(t.amount)
            from Transaction t
            group by t.merchantName
            order by sum(t.amount) desc
            """)
    List<Object[]> topMerchantsBySpend(Pageable pageable);

    @Query("""
            select coalesce(sum(t.amount), 0)
            from Transaction t
            where t.transactionDate >= :fromDate and t.transactionDate <= :toDate
            """)
    BigDecimal sumAmountBetween(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}
