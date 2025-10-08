package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Cart;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    List<Cart> findByUser(User user);
}

