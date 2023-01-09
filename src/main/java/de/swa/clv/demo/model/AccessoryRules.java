package de.swa.clv.demo.model;

import de.swa.clv.ValidationRules;
import de.swa.clv.demo.validation.ValidationRulesGettable;

// Demo on how to define rules for a class/record in a separate class
public class AccessoryRules implements ValidationRulesGettable<Accessory> {

    public static final ValidationRules<Accessory> rules = new ValidationRules<>(Accessory.class);
    static {
        rules.mandatory("name");
    }

    @Override
    public ValidationRules<Accessory> getValidationRules() {
        return rules;
    }
}
