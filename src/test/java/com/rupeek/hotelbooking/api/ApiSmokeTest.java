package com.rupeek.hotelbooking.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSmokeTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthAndSwaggerAreExposed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void onboardingValidatesAndCreatesOwner() throws Exception {
        String body = """
                {
                  "name": "Example Owner",
                  "properties": [{
                    "name": "Example Hotel",
                    "city": "Pune",
                    "locality": "Baner",
                    "starRating": 4,
                    "amenities": ["WIFI"],
                    "roomTypes": [{"name": "Deluxe", "pricePerNight": 2500, "maxGuests": 2, "inventory": 3}]
                  }]
                }
                """;

        mockMvc.perform(post("/api/v1/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void onboardedOwnerCanBeFetchedById() throws Exception {
        String body = """
                {
                  "name": "Lookup Owner",
                  "properties": [{
                    "name": "Lookup Hotel",
                    "city": "Pune",
                    "locality": "Baner",
                    "starRating": 4,
                    "amenities": ["WIFI"],
                    "roomTypes": [{"name": "Deluxe", "pricePerNight": 2500, "maxGuests": 2, "inventory": 3}]
                  }]
                }
                """;

        String response = mockMvc.perform(post("/api/v1/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ownerId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/api/v1/owners/{ownerId}", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lookup Owner"));
    }

    @Test
    void fetchingUnknownOwnerReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/owners/{ownerId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

        @Test
        void paymentRequiresIdempotencyKey() throws Exception {
                mockMvc.perform(post("/api/v1/bookings/{bookingId}/payment", UUID.randomUUID())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"method\":\"UPI\"}"))
                                .andExpect(status().isBadRequest());
        }
}