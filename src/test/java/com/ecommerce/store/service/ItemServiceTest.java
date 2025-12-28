package com.ecommerce.store.service;

import com.ecommerce.store.dto.ItemResponse;
import com.ecommerce.store.model.Item;
import com.ecommerce.store.repository.IItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ItemService Tests")
class ItemServiceTest {

    @Mock
    private IItemRepository itemRepository;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        itemService = new ItemServiceImpl(itemRepository);
    }

    @Test
    @DisplayName("Should return all items")
    void getAllItems_ReturnsAllItems() {
        // Given
        Item item1 = createTestItem(UUID.randomUUID(), "Item 1", BigDecimal.valueOf(10.00));
        Item item2 = createTestItem(UUID.randomUUID(), "Item 2", BigDecimal.valueOf(20.00));
        Item item3 = createTestItem(UUID.randomUUID(), "Item 3", BigDecimal.valueOf(30.00));

        when(itemRepository.findAll()).thenReturn(Arrays.asList(item1, item2, item3));

        // When
        List<ItemResponse> responses = itemService.getAllItems();

        // Then
        assertEquals(3, responses.size());

        assertItemResponse(responses.get(0), item1);
        assertItemResponse(responses.get(1), item2);
        assertItemResponse(responses.get(2), item3);

        verify(itemRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no items exist")
    void getAllItems_NoItems_ReturnsEmptyList() {
        // Given
        when(itemRepository.findAll()).thenReturn(List.of());

        // When
        List<ItemResponse> responses = itemService.getAllItems();

        // Then
        assertTrue(responses.isEmpty());
        verify(itemRepository).findAll();
    }

    @Test
    @DisplayName("Should return item by ID")
    void getItemById_ItemExists_ReturnsItem() {
        // Given
        UUID itemId = UUID.randomUUID();
        Item item = createTestItem(itemId, "Test Item", BigDecimal.valueOf(15.00));

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // When
        ItemResponse response = itemService.getItemById(itemId);

        // Then
        assertItemResponse(response, item);
        verify(itemRepository).findById(itemId);
    }

    @Test
    @DisplayName("Should throw exception when item not found")
    void getItemById_ItemNotFound_ThrowsException() {
        // Given
        UUID itemId = UUID.randomUUID();

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.getItemById(itemId));
        assertEquals("Item not found: " + itemId, exception.getMessage());

        verify(itemRepository).findById(itemId);
    }

    private Item createTestItem(UUID itemId, String name, BigDecimal price) {
        Item item = new Item();
        item.setItemId(itemId);
        item.setName(name);
        item.setPrice(price);
        return item;
    }

    private void assertItemResponse(ItemResponse response, Item expectedItem) {
        assertEquals(expectedItem.getItemId(), response.getItemId());
        assertEquals(expectedItem.getName(), response.getName());
        assertEquals(expectedItem.getPrice(), response.getPrice());
    }
}