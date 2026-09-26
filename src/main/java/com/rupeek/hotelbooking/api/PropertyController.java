package com.rupeek.hotelbooking.api;

import com.rupeek.hotelbooking.application.DiscoveryService;
import com.rupeek.hotelbooking.application.SearchCriteria;
import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Property;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@Tag(name = "Properties", description = "Read-only discovery of properties created via POST /api/v1/owners. "
        + "Used to find the propertyId/roomTypeId needed to create a booking.")
public class PropertyController {
    private final DiscoveryService discoveryService;

    public PropertyController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping("/{propertyId}")
    @Operation(summary = "Get a property by id",
            description = "Returns a single property and its room types. `propertyId` is one of the property ids "
                    + "returned by `POST /api/v1/owners` (or by this endpoint's search).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property found"),
            @ApiResponse(responseCode = "404", description = "No property exists with the given id")
    })
    public Property get(@PathVariable UUID propertyId) {
        return discoveryService.getProperty(propertyId);
    }

    @GetMapping("/search")
    @Operation(summary = "Search properties",
            description = "Searches properties by location, stay dates, guest count, price range, amenities and "
                    + "star rating. All parameters are optional and combined with AND; omit a parameter to skip "
                    + "that filter. `checkIn`/`checkOut` filter to room types with available inventory for that "
                    + "date range - pass both or neither. Results feed the `propertyId`/`roomTypeId` used to call "
                    + "`POST /api/v1/bookings`.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zero or more matching properties")
    })
    public List<Property> search(
            @Parameter(description = "Filter by city", example = "Bengaluru")
            @RequestParam(required = false) String city,
            @Parameter(description = "Filter by locality within the city", example = "Indiranagar")
            @RequestParam(required = false) String locality,
            @Parameter(description = "Stay start date (inclusive), ISO-8601. Required together with checkOut to filter by availability", example = "2026-10-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @Parameter(description = "Stay end date (exclusive), ISO-8601. Required together with checkIn to filter by availability", example = "2026-10-05")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @Parameter(description = "Minimum number of guests a room type must accommodate", example = "2")
            @RequestParam(required = false) Integer guests,
            @Parameter(description = "Minimum price per night, in INR", example = "1000")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price per night, in INR", example = "5000")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Property must have all of these amenities", example = "[\"wifi\",\"pool\"]")
            @RequestParam(required = false) Set<String> amenities,
            @Parameter(description = "Minimum star rating (1-5)", example = "3")
            @RequestParam(required = false) Integer minStarRating) {
        DateRange stay = checkIn == null && checkOut == null ? null : new DateRange(checkIn, checkOut);
        return discoveryService.search(new SearchCriteria(city, locality, stay, guests, minPrice, maxPrice,
                amenities, minStarRating));
    }
}