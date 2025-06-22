package com.example.ezcart.mcp.server.service;

import com.example.ezcart.mcp.server.domain.CartItem;
import com.example.ezcart.mcp.server.domain.Product;
import com.example.ezcart.mcp.server.domain.Review;
import com.example.ezcart.mcp.server.security.OAuth2TokenService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

import java.util.List;

@Service
public class EzcartService {
    private final RestClient restClient;
    private final OAuth2TokenService tokenService;

    @Autowired
    public EzcartService(
            @Value("${ezcart.api.base-url}") String baseUrl,
            OAuth2TokenService tokenService) {
        this.tokenService = tokenService;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInitializer(request -> {
                    request.getHeaders().set("Accept", "application/json");
                    request.getHeaders().set("Content-Type", "application/json");
                    request.getHeaders().setBearerAuth(tokenService.getAccessToken().replace("Bearer ", ""));
                })
                .build();
    }

//    @Tool(description = "Get all available products from the catalog")
    public List<Product> getAllProducts() {
        return restClient.get()
                .uri("/api/catalog/products")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

//    @Tool(description = "Get products filtered by category. Supported categories are: 'LAPTOP', 'MOBILE', 'TABLET'")
    public List<Product> getProductsByCategory(String category) {
        return restClient.get()
                .uri("/api/catalog/products/category/{category}", category)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Tool(description = """
        Search for products with various filters. 
        Use when you need to find products matching specific criteria.
        
        Parameters:
        - query: (Optional) Text to search in product names, descriptions and specifications. 
            This follows AND-logic keyword search. 
            It supports a Maximum of 10 words for search. Please refine your query
        - categories: (Optional) List of categories to filter by (e.g., ['LAPTOP', 'MOBILE', 'TABLET'])
        - minPrice: (Optional) Minimum price filter (e.g., 500.0)
        - maxPrice: (Optional) Maximum price filter (e.g., 1000.0)
        - manufacturers: (Optional) List of manufacturer names to filter by (e.g., ['Samsung', 'Dell'])
        - ramFilters: (Optional) List of RAM specifications to filter by (e.g., ['8GB', '16GB'])
        - processorFilters: (Optional) List of processor types to filter by (e.g., ['i5', 'i7', 'Ryzen 7'])
        - storageFilters: (Optional) List of storage specifications to filter by (e.g., ['256GB SSD', '512GB SSD'])
        
        Returns: List of products matching the criteria
        Example: search for laptops under $1000 from Dell with 16GB RAM and i7 processor
        """)
    public List<Product> searchProducts(
            String query,
            List<String> categories,
            Double minPrice,
            Double maxPrice,
            List<String> manufacturers,
            List<String> ramFilters,
            List<String> processorFilters,
            List<String> storageFilters) {

        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/catalog/search");
                    if (query != null) uriBuilder.queryParam("query", query);
                    if (categories != null && !categories.isEmpty()) {
                        categories.forEach(category -> uriBuilder.queryParam("category", category));
                    }
                    if (minPrice != null) uriBuilder.queryParam("minPrice", minPrice);
                    if (maxPrice != null) uriBuilder.queryParam("maxPrice", maxPrice);
                    if (manufacturers != null && !manufacturers.isEmpty()) {
                        manufacturers.forEach(manufacturer -> uriBuilder.queryParam("manufacturer", manufacturer));
                    }
                    if (ramFilters != null && !ramFilters.isEmpty()) {
                        ramFilters.forEach(ram -> uriBuilder.queryParam("spec.ram", ram));
                    }
                    if (processorFilters != null && !processorFilters.isEmpty()) {
                        processorFilters.forEach(proc -> uriBuilder.queryParam("spec.processor", proc));
                    }
                    if (storageFilters != null && !storageFilters.isEmpty()) {
                        storageFilters.forEach(storage -> uriBuilder.queryParam("spec.storage", storage));
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Tool(description = """
        Get detailed information about a specific product.
        
        Parameters:
        - productId: The unique identifier of the product (e.g., 'prod-123')
        
        Returns: Complete product details including specifications and availability
        Example: get details for product ID 'prod-123'
        """)
    public Product getProductById(@ToolParam String productId, ToolContext toolContext) {
        return restClient.get()
                .uri("/api/catalog/products/{id}", productId)
                .retrieve()
                .body(Product.class);
    }

    @Tool(description = """
        Get all reviews for a specific product.
        
        Parameters:
        - productId: The unique identifier of the product (e.g., 'prod-123')
        
        Returns: List of reviews including ratings and comments
        Example: get reviews for product ID 'prod-123'
        """)
    public List<Review> getProductReviews(String productId) {
        return restClient.get()
                .uri("/api/reviews/product/{productId}", productId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Tool(description = """
        Get all product reviews written by a specific user.
        
        Parameters:
        - userId: The unique identifier of the user (e.g., 'user-456')
        
        Returns: List of reviews the user has written across all products
        Example: get all reviews by user ID 'user-456'
        """)
    public List<Review> getUserReviews(String userId) {
        return restClient.get()
                .uri("/api/reviews/user/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Tool(description = "Get all items from the user's shopping cart.")
    public List<CartItem> getCart() {
        return restClient.get()
                .uri("/api/cart")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Tool(description = "Add an item to the shopping cart.")
    public List<CartItem> addToCart(String productId, int quantity) {
        CartItem item = new CartItem(productId, quantity);
        return restClient.post()
                .uri("/api/cart/items")
                .body(item)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Tool(description = "Update the quantity of an item in the shopping cart.")
    public List<CartItem> updateCartItem(String productId, int quantity) {
        return restClient.put()
                .uri("/api/cart/items/{productId}?quantity={quantity}", productId, quantity)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Tool(description = "Remove an item from the shopping cart.")
    public void removeFromCart(String productId) {
        restClient.delete()
                .uri("/api/cart/items/{productId}", productId)
                .retrieve()
                .toBodilessEntity();
    }

    @Tool(description = "Remove all items from the shopping cart.")
    public void clearCart() {
        restClient.delete()
                .uri("/api/cart")
                .retrieve()
                .toBodilessEntity();
    }
}
