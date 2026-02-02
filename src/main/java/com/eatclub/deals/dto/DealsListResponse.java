package com.eatclub.deals.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO wrapper for the list of deals returned by Task 1 API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealsListResponse {
    private List<DealResponse> deals;
}