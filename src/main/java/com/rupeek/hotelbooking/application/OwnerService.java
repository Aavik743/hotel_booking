package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.application.port.OwnerRepository;
import com.rupeek.hotelbooking.api.ResourceNotFoundException;
import com.rupeek.hotelbooking.domain.Owner;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public Owner create(Owner owner) {
        return ownerRepository.save(owner);
    }

    public Owner get(UUID id) {
        return ownerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + id));
    }
}