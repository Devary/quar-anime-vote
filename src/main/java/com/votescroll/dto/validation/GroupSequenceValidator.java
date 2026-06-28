package com.votescroll.dto.validation;

import com.votescroll.dto.MultiPollCreateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;

public class GroupSequenceValidator implements ConstraintValidator<ValidGroupSequence, MultiPollCreateDto> {

    @Override
    public boolean isValid(MultiPollCreateDto dto, ConstraintValidatorContext ctx) {
        if (dto == null || dto.groups == null || dto.groups.isEmpty()) return true;
        ctx.disableDefaultConstraintViolation();
        boolean valid = true;
        Instant now = Instant.now();

        // At least one group must start now
        boolean hasNow = dto.groups.stream().anyMatch(g -> g.startNow);
        if (!hasNow) {
            ctx.buildConstraintViolationWithTemplate("At least one group must have startNow enabled")
               .addPropertyNode("groups").addConstraintViolation();
            valid = false;
        }

        // Groups must be ordered: group[i].resolvedStart >= group[i-1].resolvedStart
        Instant prevStart = null;
        for (int i = 0; i < dto.groups.size(); i++) {
            MultiPollCreateDto.GroupCreateDto g = dto.groups.get(i);
            Instant resolvedStart = g.startNow ? now : g.startDate;
            if (resolvedStart == null) { prevStart = null; continue; } // per-group validator will catch this

            if (prevStart != null && resolvedStart.isBefore(prevStart)) {
                ctx.buildConstraintViolationWithTemplate(
                    "Group " + (i + 1) + " start date must be at or after group " + i + " start date")
                   .addPropertyNode("groups").addConstraintViolation();
                valid = false;
                break;
            }
            prevStart = resolvedStart;
        }

        return valid;
    }
}
