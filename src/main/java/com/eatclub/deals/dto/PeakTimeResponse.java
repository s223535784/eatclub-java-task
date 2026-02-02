package com.eatclub.deals.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Task 2 API response containing peak deal availability window.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeakTimeResponse {
    private String peakTimeStart;
    private String peakTimeEnd;
}