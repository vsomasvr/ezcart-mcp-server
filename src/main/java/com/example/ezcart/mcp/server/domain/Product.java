package com.example.ezcart.mcp.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Product(
    String productId,
    String productName,
    String manufacturer,
    String category,
    double price,
    String currency,
    String thumbnailUrl,
    String fullImageUrl,
    String shortDescription,
    String longDescription,
    Object specifications,
    String[] features,
    String[] availableColors,
    @JsonInclude(Include.NON_NULL)
    Object detailedSpecifications
) {
    // Empty constructor for JSON deserialization
    public Product {
    }
}
