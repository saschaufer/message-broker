package de.saschaufer.message_broker.app.agent_http.config.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.file.Path;
import java.util.List;

public class PathValidator implements ConstraintValidator<de.saschaufer.message_broker.app.agent_http.config.validator.Path, Path> {
    private boolean nullable;
    private boolean directory;
    private String[] allowedFileTypes;

    @Override
    public void initialize(final de.saschaufer.message_broker.app.agent_http.config.validator.Path constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);

        nullable = constraintAnnotation.nullable();
        directory = constraintAnnotation.directory();
        allowedFileTypes = constraintAnnotation.allowedFileTypes();
    }

    @Override
    public boolean isValid(final Path value, final ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        if (value == null) {
            return nullable;
        }

        if (!value.toFile().exists()) {
            context.buildConstraintViolationWithTemplate(String.format("The %s does not exist.", directory ? "directory" : "file")).addConstraintViolation();
            return false;
        }

        if (directory) {

            if (value.toFile().isDirectory()) {
                context.buildConstraintViolationWithTemplate("The path does not point to a directory.").addConstraintViolation();
                return false;
            }

            return true;
        }

        if (allowedFileTypes.length == 0) {
            return true;
        }

        final String filename = value.getFileName().toString();
        if (!filename.contains(".")) {
            context.buildConstraintViolationWithTemplate("The file has no type.").addConstraintViolation();
            return false;
        }

        final String type = filename.substring(filename.lastIndexOf(".") + 1);
        if (!List.of(allowedFileTypes).contains(type)) {
            context.buildConstraintViolationWithTemplate(String.format("The file type '%s' is not allowed.", type)).addConstraintViolation();
            return false;
        }

        return true;
    }
}
