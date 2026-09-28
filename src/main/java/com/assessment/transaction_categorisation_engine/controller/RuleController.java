package com.assessment.transaction_categorisation_engine.controller;


import com.assessment.transaction_categorisation_engine.dto.RuleRequest;
import com.assessment.transaction_categorisation_engine.dto.RuleResponse;
import com.assessment.transaction_categorisation_engine.service.RuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public RuleResponse createOrUpdate(@Valid @RequestBody RuleRequest request) {
        return ruleService.createOrUpdate(request);
    }
}
