package de.swa.clv.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.swa.clv.ValidationRules;
import de.swa.clv.demo.validation.ValidationRulesGettable;

public class Accessory implements ValidationRulesGettable<Accessory> {

    private String name;
    private int amount;

    // Just to demonstrate serialization of rules for multiple entities
    public static final ValidationRules<Accessory> RULES = new ValidationRules<>(Accessory.class);
    static {
        RULES.mandatory("name");
    }

    public Accessory() {
    }

    public Accessory(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @JsonIgnore
    @Override
    public ValidationRules<Accessory> getValidationRules() {
        return RULES;
    }

}
