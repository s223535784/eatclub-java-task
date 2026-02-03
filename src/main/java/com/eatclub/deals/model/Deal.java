package com.eatclub.deals.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal {

    private String objectId;
    private String discount;
    private String dineIn;
    private String lightning; // flash deal flag
    private String qtyLeft;

    // API returns either open/close or start/end for deal times
    private String open;
    private String close;
    private String start;
    private String end;

    // Returns whichever time field is available, null if neither
    public String getEffectiveOpen() {
        if (open != null)
            return open;
        if (start != null)
            return start;
        return null;
    }

    public String getEffectiveClose() {
        if (close != null)
            return close;
        if (end != null)
            return end;
        return null;
    }
}