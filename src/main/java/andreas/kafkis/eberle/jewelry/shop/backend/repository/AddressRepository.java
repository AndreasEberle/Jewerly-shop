package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Address;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserId(UUID userId);
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtAsc(UUID userId);
    List<Address> findByUserIdAndIsActiveTrue(UUID userId);
    List<Address> findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtAsc(UUID userId);
}


