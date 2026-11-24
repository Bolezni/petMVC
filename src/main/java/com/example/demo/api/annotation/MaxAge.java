package com.example.demo.api.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxAgeValidator.class)
@Documented
public @interface MaxAge {
    String message() default "Возраст не может превышать {value} лет";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int value(); // максимально допустимый возраст в годах
}
