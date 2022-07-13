package de.swa.clv.demo.validation;

import de.swa.clv.UserPermissions;
import de.swa.clv.ValidationRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@SuppressWarnings("squid:S1214")
public interface ValidationRulesCheck {

    Logger log = LoggerFactory.getLogger(ValidationRulesCheck.class);

    /**
     * Checks mandatory and content rules for the ValidationRulesGettable.
     *
         * @param object the object against which the rules are checked
     */
    default void requireValidationRulesPass(ValidationRulesGettable<? extends Object> object, String[] permissions) {
        ValidationRules<?> rules = object.getValidationRules();
        UserPermissions userPerms = UserPermissions.of(permissions);
        List<String> errors = ValidatorProvider.VALIDATOR.validateMandatoryRules(object, userPerms, rules);
        errors.addAll(ValidatorProvider.VALIDATOR.validateContentRules(object, userPerms, rules));
        if (!errors.isEmpty()) {
            log.info("Validation rule errors detected (during insert): {}", errors);
            throw new ValidationException(errors.toString(), errors);
        }
    }

    /**
     * Checks mandatory, immutable content and update rules for the ValidationRulesGettable.
     *
     * @param editedObject  the edited object against which the rules are checked
     * @param currentObject the current object against which the immutable and update rules are checked
     */
    default void requireValidationRulesPass(ValidationRulesGettable<? extends Object> editedObject,
            ValidationRulesGettable<? extends Object> currentObject, String[] permissions) {
        ValidationRules<?> rules = currentObject.getValidationRules();
        UserPermissions userPerms = UserPermissions.of(permissions);
        List<String> errors = ValidatorProvider.VALIDATOR.validateMandatoryRules(editedObject, userPerms, rules);
        errors.addAll(ValidatorProvider.VALIDATOR.validateContentRules(editedObject, userPerms, rules));
        errors.addAll(ValidatorProvider.VALIDATOR.validateImmutableRules(currentObject, editedObject, userPerms, rules));
        errors.addAll(ValidatorProvider.VALIDATOR.validateUpdateRules(currentObject, editedObject, userPerms, rules));
        if (!errors.isEmpty()) {
            log.info("Validation rule errors detected (during update): {}", errors);
            throw new ValidationException(errors.toString(), errors);
        }
    }

}
