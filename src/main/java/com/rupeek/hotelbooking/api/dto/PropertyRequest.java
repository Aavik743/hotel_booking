package com.rupeek.hotelbooking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "A property belonging to an owner, with its location and room types. Only created as part of POST /api/v1/owners.")
public record PropertyRequest(
        @Schema(description = "Property display name", example = "Sea View Villa")
        @NotBlank String name,
        @Schema(description = "City the property is in, used as a search filter by GET /api/v1/properties/search", example = "Bengaluru")
        @NotBlank String city,
        @Schema(description = "Locality/neighbourhood within the city, used as a search filter", example = "Indiranagar")
        @NotBlank String locality,
        @Schema(description = "Star rating from 1 to 5", example = "4")
        @Min(1) @Max(5) int starRating,
        @Schema(description = "Amenity tags, used as a search filter", example = "[\"wifi\", \"pool\", \"parking\"]")
        List<String> amenities,
        @Schema(description = "At least one room type offered by this property")
        @NotEmpty List<@Valid RoomTypeRequest> roomTypes) {
}