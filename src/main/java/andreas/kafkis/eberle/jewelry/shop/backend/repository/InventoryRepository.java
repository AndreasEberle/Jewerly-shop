package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {}


