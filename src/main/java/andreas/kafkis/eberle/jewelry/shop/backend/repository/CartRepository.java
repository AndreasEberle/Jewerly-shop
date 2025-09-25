package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Cart;

public interface CartRepository extends JpaRepository<Cart, UUID> {}


