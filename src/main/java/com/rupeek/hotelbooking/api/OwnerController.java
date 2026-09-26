package com.rupeek.hotelbooking.api;

import com.rupeek.hotelbooking.api.dto.OwnerRequest;
import com.rupeek.hotelbooking.api.dto.PropertyRequest;
import com.rupeek.hotelbooking.api.dto.RoomTypeRequest;
import com.rupeek.hotelbooking.application.OwnerService;
import com.rupeek.hotelbooking.domain.Location;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.Owner;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.domain.RoomType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owners")
@Tag(name = "Owners", description = "Onboard property owners along with their properties and room types. "
        + "The propertyId/roomTypeId values returned here are the ones consumed by the search and booking APIs.")
public class OwnerController {
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Onboard an owner with their properties",
            description = "Creates an owner along with a nested list of properties and, for each property, a nested "
                    + "list of room types. This is the source of the `propertyId` and `roomTypeId` values later used "
                    + "in `GET /api/v1/properties/search`, `GET /api/v1/properties/{propertyId}` and "
                    + "`POST /api/v1/bookings`; there is no separate endpoint to create a property or room type "
                    + "independently.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Owner created with generated ids for the owner, its properties and room types"),
            @ApiResponse(responseCode = "400", description = "Validation failure, e.g. missing name or empty properties/roomTypes list")
    })
    public Owner create(@Valid @RequestBody OwnerRequest request) {
        List<Property> properties = request.properties().stream().map(this::property).toList();
        return ownerService.create(new Owner(UUID.randomUUID(), request.name(), properties));
    }

    @GetMapping("/{ownerId}")
    @Operation(summary = "Get an owner by id",
            description = "Returns an owner and their properties/room types. `ownerId` is the `id` returned by the create-owner call.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Owner found"),
            @ApiResponse(responseCode = "404", description = "No owner exists with the given id")
    })
    public Owner get(@PathVariable UUID ownerId) {
        return ownerService.get(ownerId);
    }

    private Property property(PropertyRequest request) {
        List<RoomType> roomTypes = request.roomTypes().stream().map(this::roomType).toList();
        return new Property(UUID.randomUUID(), request.name(), new Location(request.city(), request.locality()),
                request.starRating(), request.amenities(), roomTypes);
    }

    private RoomType roomType(RoomTypeRequest request) {
        return new RoomType(UUID.randomUUID(), request.name(), new Money(request.pricePerNight(), "INR"),
                request.maxGuests(), request.inventory());
    }
}