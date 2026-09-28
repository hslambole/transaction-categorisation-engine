package com.assessment.transaction_categorisation_engine.service;

import com.assessment.transaction_categorisation_engine.domain.MerchantCategory;
import com.assessment.transaction_categorisation_engine.domain.Source;
import com.assessment.transaction_categorisation_engine.domain.Transaction;
import com.assessment.transaction_categorisation_engine.dto.*;
import com.assessment.transaction_categorisation_engine.repository.MerchantCategoryRepository;
import com.assessment.transaction_categorisation_engine.repository.TransactionRepository;
import com.assessment.transaction_categorisation_engine.service.CurrencyValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final MerchantCategoryRepository merchantCategoryRepository;
    private final Validator validator;
    private final CurrencyValidator currencyValidator;

    public TransactionService(
            TransactionRepository transactionRepository,
            MerchantCategoryRepository merchantCategoryRepository,
            Validator validator,
            CurrencyValidator currencyValidator
    ) {
        this.transactionRepository = transactionRepository;
        this.merchantCategoryRepository = merchantCategoryRepository;
        this.validator = validator;
        this.currencyValidator = currencyValidator;
    }

    @Transactional
    public BulkIngestResponse ingest(List<TransactionIngestRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new BulkIngestResponse(0, 0, List.of());
        }

        int accepted = 0;
        List<RejectedTransaction> errors = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            TransactionIngestRequest request = requests.get(i);
            String rejection = validateItem(request);
            if (rejection != null) {
                errors.add(new RejectedTransaction(i, rejection));
                continue;
            }

            String currency = request.currency().toUpperCase();
            String merchantName = request.merchantName().trim();

            boolean duplicate = transactionRepository
                    .existsByMerchantNameAndAmountAndCurrencyAndTransactionDateAndSource(
                            merchantName,
                            request.amount(),
                            currency,
                            request.transactionDate(),
                            request.source()
                    );
            if (duplicate) {
                errors.add(new RejectedTransaction(i, "duplicate transaction"));
                continue;
            }

            Transaction transaction = new Transaction();
            transaction.setMerchantName(merchantName);
            transaction.setAmount(request.amount());
            transaction.setCurrency(currency);
            transaction.setTransactionDate(request.transactionDate());
            transaction.setSource(request.source());
            transactionRepository.save(transaction);
            accepted++;
        }

        return new BulkIngestResponse(accepted, errors.size(), errors);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> list(
            String category,
            Source source,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate").and(Sort.by("id")));
        Page<Transaction> result = transactionRepository.search(category, source, from, to, pageable);

        List<String> merchantNames = result.getContent().stream()
                .map(Transaction::getMerchantName)
                .distinct()
                .toList();

        Map<String, MerchantCategory> categories = merchantCategoryRepository.findByMerchantNameIn(merchantNames)
                .stream()
                .collect(Collectors.toMap(MerchantCategory::getMerchantName, Function.identity()));

        List<TransactionResponse> content = result.getContent().stream()
                .map(tx -> toResponse(tx, categories.get(tx.getMerchantName())))
                .toList();

        return new PagedResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private String validateItem(TransactionIngestRequest request) {
        if (request == null) {
            return "request item is null";
        }

        Set<ConstraintViolation<TransactionIngestRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            return violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
        }

        if (!currencyValidator.isKnownCurrency(request.currency())) {
            return "unknown currency";
        }

        return null;
    }

    private TransactionResponse toResponse(Transaction tx, MerchantCategory category) {
        return new TransactionResponse(
                tx.getId(),
                tx.getMerchantName(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getTransactionDate(),
                tx.getSource(),
                tx.getCreatedAt(),
                category == null ? null : category.getCategory(),
                category == null ? null : category.getSubCategory()
        );
    }
}
