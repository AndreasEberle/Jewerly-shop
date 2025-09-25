package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {}


