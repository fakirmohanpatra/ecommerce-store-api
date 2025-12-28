package com.ecommerce.store.service;

import com.ecommerce.store.dto.ItemResponse;
import com.ecommerce.store.model.Item;
import com.ecommerce.store.repository.IItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for Item (product catalog) operations.
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    
    private final IItemRepository itemRepository;
    
    @Override
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::toItemResponse)
                .toList();
    }
    
    @Override
    public ItemResponse getItemById(UUID itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
        return toItemResponse(item);
    }
    
    /**
     * Convert Item entity to ItemResponse DTO.
     */
    private ItemResponse toItemResponse(Item item) {
        return new ItemResponse(
                item.getItemId(),
                item.getName(),
                item.getPrice()
        );
    }
}
