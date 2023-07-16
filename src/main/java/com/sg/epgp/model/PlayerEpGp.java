package com.sg.epgp.model;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder (toBuilder = true)
public class PlayerEpGp {
    @CsvBindByName
    private String name;
    @CsvBindByName
    private BigDecimal effortPoints;
    @CsvBindByName
    private BigDecimal gearPoints;
}
