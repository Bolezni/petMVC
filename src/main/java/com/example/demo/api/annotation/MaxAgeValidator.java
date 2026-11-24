package com.example.demo.api.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class MaxAgeValidator implements ConstraintValidator<MaxAge, LocalDate> {

    private int maxAge;

    @Override
    public void initialize(MaxAge annotation) {
        this.maxAge = annotation.value();
    }

    @Override
    public boolean isValid(LocalDate birthday, ConstraintValidatorContext context) {
        if (birthday == null) {
            return true;
        }
        int age = Period.between(birthday, LocalDate.now()).getYears();
        return age <= maxAge;
    }
}
