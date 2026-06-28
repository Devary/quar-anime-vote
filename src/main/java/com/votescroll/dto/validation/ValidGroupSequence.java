package com.votescroll.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GroupSequenceValidator.class)
@Documented
public @interface ValidGroupSequence {
    String message() default "Group voting periods are invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
