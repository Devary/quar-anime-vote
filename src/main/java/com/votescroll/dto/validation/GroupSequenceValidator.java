package com.votescroll.dto.validation;

import com.votescroll.dto.MultiPollCreateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class GroupSequenceValidator implements ConstraintValidator<ValidGroupSequence, MultiPollCreateDto> {

    @Override
    public boolean isValid(MultiPollCreateDto dto, ConstraintValidatorContext ctx) {
        if (dto == null || dto.groups == null || dto.groups.isEmpty()) return true;
        ctx.disableDefaultConstraintViolation();
        boolean valid = true;
        Instant now = Instant.now();

        // Only level-0 groups participate in the sequence check
        List<MultiPollCreateDto.GroupCreateDto> baseGroups = dto.groups.stream()
            .filter(g -> g.level == 0)
            .collect(Collectors.toList());

        if (baseGroups.isEmpty()) {
            ctx.buildConstraintViolationWithTemplate("At least one level-0 group is required")
               .addPropertyNode("groups").addConstraintViolation();
            return false;
        }

        boolean hasNow = baseGroups.stream().anyMatch(g -> g.startNow);
        if (!hasNow) {
            ctx.buildConstraintViolationWithTemplate("At least one group must have startNow enabled")
               .addPropertyNode("groups").addConstraintViolation();
            valid = false;
        }

        Instant prevStart = null;
        for (int i = 0; i < baseGroups.size(); i++) {
            MultiPollCreateDto.GroupCreateDto g = baseGroups.get(i);
            Instant resolvedStart = g.startNow ? now : g.startDate;
            if (resolvedStart == null) { prevStart = null; continue; }

            if (prevStart != null && resolvedStart.isBefore(prevStart)) {
                ctx.buildConstraintViolationWithTemplate(
                    "Base group " + (i + 1) + " start date must be at or after base group " + i + " start date")
                   .addPropertyNode("groups").addConstraintViolation();
                valid = false;
                break;
            }
            prevStart = resolvedStart;
        }

        // Validate feeder indices for level 1+ groups
        for (int i = 0; i < dto.groups.size(); i++) {
            MultiPollCreateDto.GroupCreateDto g = dto.groups.get(i);
            if (g.level > 0) {
                if (g.feederIndices == null || g.feederIndices.size() < 2) {
                    ctx.buildConstraintViolationWithTemplate(
                        "Bracket group " + (i + 1) + " must reference at least 2 feeder groups")
                       .addPropertyNode("groups").addConstraintViolation();
                    valid = false;
                } else {
                    for (int fi : g.feederIndices) {
                        if (fi < 0 || fi >= dto.groups.size() || fi == i) {
                            ctx.buildConstraintViolationWithTemplate(
                                "Bracket group " + (i + 1) + " has invalid feeder index: " + fi)
                               .addPropertyNode("groups").addConstraintViolation();
                            valid = false;
                            break;
                        }
                    }
                }
            }
        }

        return valid;
    }
}
