package com.votescroll.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GroupPeriodValidator.class)
@Documented
public @interface ValidGroupPeriod {
    String message() default "Invalid group voting period";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
