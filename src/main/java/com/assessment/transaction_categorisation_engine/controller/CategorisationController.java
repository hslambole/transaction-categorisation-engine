package com.assessment.transaction_categorisation_engine.controller;

import com.assessment.transaction_categorisation_engine.dto.TriggerResponse;
import com.assessment.transaction_categorisation_engine.service.CategorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categorise")
public class CategorisationController {

    private final CategorizationService categorizationService;

    public CategorisationController(CategorizationService categorizationService) {
        this.categorizationService = categorizationService;
    }

    @PostMapping("/trigger")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TriggerResponse trigger() {
        categorizationService.triggerAsync();
        return new TriggerResponse("TRIGGERED");
    }
}
