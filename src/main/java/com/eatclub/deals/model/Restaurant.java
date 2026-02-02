package com.eatclub.deals.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model class representing a restaurant from the external API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {
    
    private String objectId;
    private String name;
    private String address1;
    private String suburb;
    private List<String> cuisines;
    private String imageLink;
    private String open;
    private String close;
    private List<Deal> deals;
}