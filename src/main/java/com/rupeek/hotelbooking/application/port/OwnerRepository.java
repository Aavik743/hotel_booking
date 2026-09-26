package com.rupeek.hotelbooking.application.port;

import com.rupeek.hotelbooking.domain.Owner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OwnerRepository {
    Owner save(Owner owner);
    Optional<Owner> findById(UUID id);
    List<Owner> findAll();
}