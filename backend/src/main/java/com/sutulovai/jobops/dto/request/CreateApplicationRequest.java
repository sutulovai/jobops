package com.sutulovai.jobops.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateApplicationRequest(
        @NotNull UUID vacancyId,
        UUID cvId,
        String applicationChannel,
        String notes,
        String cityCategory
) {
}
