package com.assessment.transaction_categorisation_engine.controller;

import com.assessment.transaction_categorisation_engine.domain.Source;
import com.assessment.transaction_categorisation_engine.dto.BulkIngestResponse;
import com.assessment.transaction_categorisation_engine.dto.PagedResponse;
import com.assessment.transaction_categorisation_engine.dto.TransactionIngestRequest;
import com.assessment.transaction_categorisation_engine.dto.TransactionResponse;
import com.assessment.transaction_categorisation_engine.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.OK)
    public BulkIngestResponse bulk(@RequestBody List<TransactionIngestRequest> requests) {
        return transactionService.ingest(requests);
    }

    @GetMapping
    public PagedResponse<TransactionResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Source source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return transactionService.list(category, source, from, to, page, size);
    }
}
