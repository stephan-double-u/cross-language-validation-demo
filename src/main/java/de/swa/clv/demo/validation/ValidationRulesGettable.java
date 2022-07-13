package de.swa.clv.demo.validation;

import de.swa.clv.ValidationRules;

public interface ValidationRulesGettable<T> {

    ValidationRules<T> getValidationRules();

}
