package com.ecommerce.store.repository;

import com.ecommerce.store.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemRepository Tests")
class ItemRepositoryTest {

    private ItemRepository itemRepository;
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        itemRepository = new ItemRepository(dataStore);
    }

    @Test
    @DisplayName("Should return all items from data store")
    void findAll_ReturnsAllItems() {
        // Given
        Item item1 = createTestItem("Laptop", BigDecimal.valueOf(999.99));
        Item item2 = createTestItem("Mouse", BigDecimal.valueOf(29.99));
        dataStore.items.put(item1.getItemId(), item1);
        dataStore.items.put(item2.getItemId(), item2);

        // When
        List<Item> items = itemRepository.findAll();

        // Then
        assertEquals(2, items.size());
        assertTrue(items.contains(item1));
        assertTrue(items.contains(item2));
    }

    @Test
    @DisplayName("Should return empty list when no items exist")
    void findAll_NoItems_ReturnsEmptyList() {
        // When
        List<Item> items = itemRepository.findAll();

        // Then
        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Should find item by ID when exists")
    void findById_ItemExists_ReturnsItem() {
        // Given
        Item item = createTestItem("Laptop", BigDecimal.valueOf(999.99));
        dataStore.items.put(item.getItemId(), item);

        // When
        Optional<Item> result = itemRepository.findById(item.getItemId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(item, result.get());
    }

    @Test
    @DisplayName("Should return empty when item ID doesn't exist")
    void findById_ItemNotExists_ReturnsEmpty() {
        // When
        Optional<Item> result = itemRepository.findById(UUID.randomUUID());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should save item to data store")
    void save_Item_SavesToDataStore() {
        // Given
        Item item = createTestItem("Keyboard", BigDecimal.valueOf(79.99));

        // When
        itemRepository.save(item);

        // Then
        assertEquals(item, dataStore.items.get(item.getItemId()));
    }

    @Test
    @DisplayName("Should return true when item exists")
    void existsById_ItemExists_ReturnsTrue() {
        // Given
        Item item = createTestItem("Monitor", BigDecimal.valueOf(299.99));
        dataStore.items.put(item.getItemId(), item);

        // When
        boolean exists = itemRepository.exists(item.getItemId());

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when item doesn't exist")
    void existsById_ItemNotExists_ReturnsFalse() {
        // When
        boolean exists = itemRepository.exists(UUID.randomUUID());

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should decrease item stock and show out of stock when quantity reaches zero")
    void decreaseStock_ItemStockDecreasesAndOutOfStockWhenZero() {
        // Given
        Item item = createTestItem("Headphones", BigDecimal.valueOf(199.99));
        item.setStock(2);
        dataStore.items.put(item.getItemId(), item);

        // When
        itemRepository.decreaseStock(item.getItemId());
        itemRepository.decreaseStock(item.getItemId());

        // Then
        assertEquals(0, dataStore.items.get(item.getItemId()).getStock());
        assertTrue(dataStore.items.get(item.getItemId()).isOutOfStock());
    }

    private Item createTestItem(String name, BigDecimal price) {
        Item item = new Item();
        item.setItemId(UUID.randomUUID());
        item.setName(name);
        item.setPrice(price);
        return item;
    }
}