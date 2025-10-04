package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.CurrencyRate;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, UUID> {
    
    Optional<CurrencyRate> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);
    
    @Query("SELECT cr FROM CurrencyRate cr WHERE cr.fromCurrency = :fromCurrency AND cr.toCurrency = :toCurrency")
    Optional<CurrencyRate> findRate(@Param("fromCurrency") String fromCurrency, @Param("toCurrency") String toCurrency);
}
