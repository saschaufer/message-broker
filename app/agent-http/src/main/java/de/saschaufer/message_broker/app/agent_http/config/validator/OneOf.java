package de.saschaufer.message_broker.app.agent_http.config.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = OneOfValidator.class)
@Target({FIELD, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Repeatable(OneOf.List.class)
public @interface OneOf {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] allowableValues();

    @Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OneOf[] value();
    }
}
