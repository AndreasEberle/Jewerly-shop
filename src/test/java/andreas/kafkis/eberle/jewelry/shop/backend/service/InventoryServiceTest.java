package andreas.kafkis.eberle.jewelry.shop.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Inventory;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.InventoryRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Setup Product
        testProduct = new Product();
        testProduct.setId(UUID.randomUUID());
        testProduct.setName("Diamond Ring");
        testProduct.setSku("RING001");

        // Setup Inventory
        testInventory = new Inventory();
        testInventory.setProduct(testProduct);
        testInventory.setQuantity(100);
        testInventory.setReserved(10);
    }

    @Test
    void getInventoryByProductId_WhenProductExists_ShouldReturnInventory() {
        // Given
        UUID productId = testProduct.getId();
        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(testInventory));

        // When
        Optional<Inventory> inventory = inventoryService.getInventoryByProductId(productId);

        // Then
        assertThat(inventory).isPresent();
        assertThat(inventory.get().getProduct().getId()).isEqualTo(productId);
        assertThat(inventory.get().getQuantity()).isEqualTo(100);
        assertThat(inventory.get().getReserved()).isEqualTo(10);
    }

    @Test
    void addStock_WhenValidAmount_ShouldAddStock() {
        // Given
        UUID productId = testProduct.getId();
        int amountToAdd = 50;
        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        Inventory updatedInventory = inventoryService.addStock(productId, amountToAdd);

        // Then
        assertThat(updatedInventory).isNotNull();
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void removeStock_WhenSufficientStock_ShouldRemoveStock() {
        // Given
        UUID productId = testProduct.getId();
        int amountToRemove = 20;
        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        Inventory updatedInventory = inventoryService.removeStock(productId, amountToRemove);

        // Then
        assertThat(updatedInventory).isNotNull();
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void setStock_WhenValidAmount_ShouldSetStock() {
        // Given
        UUID productId = testProduct.getId();
        int newQuantity = 200;
        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        Inventory updatedInventory = inventoryService.setStock(productId, newQuantity);

        // Then
        assertThat(updatedInventory).isNotNull();
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void reserveStock_WhenSufficientStock_ShouldReserveStock() {
        // Given
        UUID productId = testProduct.getId();
        int amountToReserve = 20;
        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        Inventory updatedInventory = inventoryService.reserveStock(productId, amountToReserve);

        // Then
        assertThat(updatedInventory).isNotNull();
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void getLowStockProducts_ShouldReturnLowStockProducts() {
        // Given
        int threshold = 20;
        when(inventoryRepository.findLowStockProducts(threshold)).thenReturn(List.of(testInventory));

        // When
        List<Inventory> lowStockProducts = inventoryService.getLowStockProducts(threshold);

        // Then
        assertThat(lowStockProducts).hasSize(1);
        assertThat(lowStockProducts.get(0)).isEqualTo(testInventory);
    }

    @Test
    void getOutOfStockProducts_ShouldReturnOutOfStockProducts() {
        // Given
        when(inventoryRepository.findOutOfStockProducts()).thenReturn(List.of(testInventory));

        // When
        List<Inventory> outOfStockProducts = inventoryService.getOutOfStockProducts();

        // Then
        assertThat(outOfStockProducts).hasSize(1);
        assertThat(outOfStockProducts.get(0)).isEqualTo(testInventory);
    }

    @Test
    void getAvailableStock_ShouldReturnCorrectAmount() {
        // Given
        UUID productId = testProduct.getId();
        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(testInventory));

        // When
        int availableStock = inventoryService.getAvailableStock(productId);

        // Then
        assertThat(availableStock).isEqualTo(90); // 100 - 10 (quantity - reserved)
    }
}
