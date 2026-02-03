package com.eatclub.deals.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// response object sent back to client for each deal
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealResponse {

    private String restaurantObjectId;
    private String restaurantName;
    private String restaurantAddress1;

    @JsonProperty("restarantSuburb")
    private String restarantSuburb;

    private String restaurantOpen;
    private String restaurantClose;
    private String dealObjectId;
    private String discount;
    private String dineIn;
    private String lightning;
    private String qtyLeft;
}