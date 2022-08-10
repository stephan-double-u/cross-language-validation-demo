package de.swa.clv.demo.validation;

import java.util.List;

/**
 * Abstract super class of all exceptions that need to transport field data to the frontend.
 */
public class ValidationException extends RuntimeException {

    // contains the name of the field with error and a message code
    private final  List<String> fieldErrors;

    public ValidationException(String message, List<String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public List<String> getFieldErrors() {
        return fieldErrors;
    }

}
