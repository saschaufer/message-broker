package de.saschaufer.message_broker.app.agent_http.config.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = PathValidator.class)
@Target({FIELD, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Repeatable(Path.List.class)
public @interface Path {

    String message() default "The path is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean nullable() default true;

    boolean directory() default false;

    String[] allowedFileTypes() default {};

    @Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        Path[] value();
    }
}
