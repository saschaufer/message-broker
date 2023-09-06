package de.saschaufer.message_broker.app.agent_http.config.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OneOfValidator implements ConstraintValidator<OneOf, String> {

    private List<String> allowableValues;

    @Override
    public void initialize(final OneOf constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        allowableValues = Arrays.asList(constraintAnnotation.allowableValues());
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        if (value == null) {
            return true;
        }

        if (value.isBlank()) {
            context.buildConstraintViolationWithTemplate("the value must not be blank").addConstraintViolation();
            return false;
        }

        if (!allowableValues.contains(value)) {
            context.buildConstraintViolationWithTemplate(String.format("the value must be one of the allowed values: %s", allowableValues.stream().collect(Collectors.joining(", ")))).addConstraintViolation();
            return false;
        }

        return true;
    }
}
