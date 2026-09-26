package com.rupeek.hotelbooking.infrastructure;

import com.rupeek.hotelbooking.application.port.OwnerRepository;
import com.rupeek.hotelbooking.domain.Owner;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOwnerRepository implements OwnerRepository {
    private final ConcurrentHashMap<UUID, Owner> owners = new ConcurrentHashMap<>();

    @Override
    public Owner save(Owner owner) {
        owners.put(owner.id(), owner);
        return owner;
    }

    @Override
    public Optional<Owner> findById(UUID id) {
        return Optional.ofNullable(owners.get(id));
    }

    @Override
    public List<Owner> findAll() {
        return List.copyOf(owners.values());
    }
}