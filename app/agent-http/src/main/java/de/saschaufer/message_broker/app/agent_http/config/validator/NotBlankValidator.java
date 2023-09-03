package de.saschaufer.message_broker.app.agent_http.config.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankValidator implements ConstraintValidator<NotBlank, String> {
    private boolean nullable;

    @Override
    public void initialize(final NotBlank constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);

        nullable = constraintAnnotation.nullable();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        if (value == null && nullable) {
            return true;
        }

        if (value == null && !nullable) {
            context.buildConstraintViolationWithTemplate("must not be null").addConstraintViolation();
            return false;
        }

        if (value.isBlank()) {
            context.buildConstraintViolationWithTemplate("must not be blank").addConstraintViolation();
            return false;
        }

        return true;
    }
}
