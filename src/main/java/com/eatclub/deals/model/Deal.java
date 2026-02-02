package com.eatclub.deals.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class representing a restaurant deal from the external API.
 * Handles both 'open/close' and 'start/end' time field variations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal {
    
    private String objectId;
    private String discount;
    private String dineIn;
    private String lightning;
    private String qtyLeft;
    
    // Some deals use 'open/close', others use 'start/end'
    private String open;
    private String close;
    private String start;
    private String end;
    
    /**
     * Gets the effective opening time for the deal.
     * Falls back to restaurant hours if not specified.
     */
    public String getEffectiveOpen() {
        if (open != null) return open;
        if (start != null) return start;
        return null; // Will use restaurant hours
    }
    
    /**
     * Gets the effective closing time for the deal.
     * Falls back to restaurant hours if not specified.
     */
    public String getEffectiveClose() {
        if (close != null) return close;
        if (end != null) return end;
        return null; // Will use restaurant hours
    }
}