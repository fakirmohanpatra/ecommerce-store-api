package com.ecommerce.store.controller;

import com.ecommerce.store.dto.ItemResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API for Item (product catalog) operations.
 * 
 * API Contract:
 * - GET /api/items     - List all items (product catalog)
 * - GET /api/items/{id} - Get item details
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {
    
    /**
     * Get all items (product catalog).
     * 
     * GET /api/items
     * 
     * Response: List<ItemResponse>
     * [
     *   {
     *     "itemId": "uuid",
     *     "name": "Laptop",
     *     "price": 999.99
     *   },
     *   ...
     * ]
     * 
     * Note: Users need to see available products before adding to cart.
     */
    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get item by ID.
     * 
     * GET /api/items/{itemId}
     * 
     * Response: ItemResponse or 404 Not Found
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable UUID itemId) {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
}
