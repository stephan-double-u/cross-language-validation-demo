package de.swa.clv.demo.validation;

import de.swa.clv.Validator;

public class ValidatorProvider {

    public static final Validator VALIDATOR = Validator.instance();
    static {
        // These are the default prefixes
        VALIDATOR.setDefaultMandatoryMessagePrefix("error.validation.mandatory.");
        VALIDATOR.setDefaultImmutableMessagePrefix("error.validation.immutable.");
        VALIDATOR.setDefaultContentMessagePrefix("error.validation.content.");
        VALIDATOR.setDefaultUpdateMessagePrefix("error.validation.update.");
    }

    private ValidatorProvider() {
        throw new IllegalStateException("Not meant to be instantiated");
    }

}
