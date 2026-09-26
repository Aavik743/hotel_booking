package com.rupeek.hotelbooking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request to onboard an owner together with all of their properties and room types in one call.")
public record OwnerRequest(
        @Schema(description = "Owner's display name", example = "Coastal Retreats Pvt Ltd")
        @NotBlank String name,
        @Schema(description = "At least one property owned by this owner")
        @NotEmpty List<@Valid PropertyRequest> properties) {
}