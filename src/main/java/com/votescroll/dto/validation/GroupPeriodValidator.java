package com.votescroll.dto.validation;

import com.votescroll.dto.MultiPollCreateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class GroupPeriodValidator implements ConstraintValidator<ValidGroupPeriod, MultiPollCreateDto.GroupCreateDto> {

    @Override
    public boolean isValid(MultiPollCreateDto.GroupCreateDto dto, ConstraintValidatorContext ctx) {
        if (dto == null) return true;
        ctx.disableDefaultConstraintViolation();
        boolean valid = true;
        Instant now = Instant.now();

        // Level 1+ bracket groups: startNow is not applicable; startDate is required explicitly
        if (dto.level > 0) {
            if (dto.startDate == null) {
                ctx.buildConstraintViolationWithTemplate("startDate is required for bracket groups")
                   .addPropertyNode("startDate").addConstraintViolation();
                valid = false;
            }
            if (dto.endDate == null) {
                ctx.buildConstraintViolationWithTemplate("endDate is required")
                   .addPropertyNode("endDate").addConstraintViolation();
                valid = false;
            }
            if (dto.startDate != null && dto.endDate != null && !dto.endDate.isAfter(dto.startDate)) {
                ctx.buildConstraintViolationWithTemplate("endDate must be after startDate")
                   .addPropertyNode("endDate").addConstraintViolation();
                valid = false;
            }
            return valid;
        }

        // Level 0: original validation
        if (!dto.startNow && dto.startDate == null) {
            ctx.buildConstraintViolationWithTemplate("startDate is required when startNow is false")
               .addPropertyNode("startDate").addConstraintViolation();
            valid = false;
        }

        if (!dto.startNow && dto.startDate != null && dto.startDate.isBefore(now.minusSeconds(60))) {
            ctx.buildConstraintViolationWithTemplate("startDate cannot be in the past")
               .addPropertyNode("startDate").addConstraintViolation();
            valid = false;
        }

        if (dto.endDate == null) {
            ctx.buildConstraintViolationWithTemplate("endDate is required")
               .addPropertyNode("endDate").addConstraintViolation();
            valid = false;
        }

        if (dto.endDate != null) {
            Instant resolvedStart = dto.startNow ? now : (dto.startDate != null ? dto.startDate : now);
            if (!dto.endDate.isAfter(resolvedStart)) {
                ctx.buildConstraintViolationWithTemplate("endDate must be after startDate")
                   .addPropertyNode("endDate").addConstraintViolation();
                valid = false;
            } else if (dto.endDate.isAfter(resolvedStart.plus(90, ChronoUnit.DAYS))) {
                ctx.buildConstraintViolationWithTemplate("endDate cannot exceed 90 days from startDate")
                   .addPropertyNode("endDate").addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
