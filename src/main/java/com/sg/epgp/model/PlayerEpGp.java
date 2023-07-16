package com.sg.epgp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder (toBuilder = true)
public class PlayerEpGp {
    private String name;
    private BigDecimal effortPoints;

    private BigDecimal gearPoints;
}
