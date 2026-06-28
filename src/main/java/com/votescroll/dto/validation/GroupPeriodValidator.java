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

        // startDate required when not startNow
        if (!dto.startNow && dto.startDate == null) {
            ctx.buildConstraintViolationWithTemplate("startDate is required when startNow is false")
               .addPropertyNode("startDate").addConstraintViolation();
            valid = false;
        }

        // manual startDate must not be in the past (60-second tolerance for network round-trip)
        if (!dto.startNow && dto.startDate != null && dto.startDate.isBefore(now.minusSeconds(60))) {
            ctx.buildConstraintViolationWithTemplate("startDate cannot be in the past")
               .addPropertyNode("startDate").addConstraintViolation();
            valid = false;
        }

        // endDate always required
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
