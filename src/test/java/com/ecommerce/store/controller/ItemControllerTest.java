package com.ecommerce.store.controller;

import com.ecommerce.store.dto.ItemResponse;
import com.ecommerce.store.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@DisplayName("Item Controller Tests")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    @DisplayName("Should return all items successfully")
    void getAllItems_ReturnsItemsList() throws Exception {
        // Given
        UUID itemId1 = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();
        List<ItemResponse> items = Arrays.asList(
                new ItemResponse(itemId1, "Laptop", BigDecimal.valueOf(999.99), 10),
                new ItemResponse(itemId2, "Smartphone", BigDecimal.valueOf(699.99), 25)
        );
        when(itemService.getAllItems()).thenReturn(items);

        // When & Then
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].itemId").value(itemId1.toString()))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].price").value(999.99))
                .andExpect(jsonPath("$[0].stock").value(10))
                .andExpect(jsonPath("$[1].itemId").value(itemId2.toString()))
                .andExpect(jsonPath("$[1].name").value("Smartphone"))
                .andExpect(jsonPath("$[1].price").value(699.99))
                .andExpect(jsonPath("$[1].stock").value(25));
    }

    @Test
    @DisplayName("Should return empty list when no items exist")
    void getAllItems_EmptyList_ReturnsEmptyArray() throws Exception {
        // Given
        List<ItemResponse> emptyItems = Arrays.asList();
        when(itemService.getAllItems()).thenReturn(emptyItems);

        // When & Then
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should return item by ID successfully")
    void getItemById_Exists_ReturnsItem() throws Exception {
        // Given
        UUID itemId = UUID.randomUUID();
        ItemResponse item = new ItemResponse(itemId, "Wireless Headphones", BigDecimal.valueOf(199.99), 15);
        when(itemService.getItemById(itemId)).thenReturn(item);

        // When & Then
        mockMvc.perform(get("/api/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.itemId").value(itemId.toString()))
                .andExpect(jsonPath("$.name").value("Wireless Headphones"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.stock").value(15));
    }

    @Test
    @DisplayName("Should return 400 for invalid UUID format")
    void getItemById_InvalidUUID_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/items/{itemId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}