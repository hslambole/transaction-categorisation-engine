package com.assessment.transaction_categorisation_engine.service;

import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Locale;

@Component
public class CurrencyValidator {

    public boolean isKnownCurrency(String code) {
        if (code == null || code.length() != 3) {
            return false;
        }
        try {
            Currency.getInstance(code.toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
