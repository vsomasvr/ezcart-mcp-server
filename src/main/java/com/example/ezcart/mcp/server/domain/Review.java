package com.example.ezcart.mcp.server.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record Review(
    String reviewId,
    String productId,
    String userId,
    int rating,
    String title,
    String comment,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date
) {}
