package com.sg.epgp.model;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ItemGP {
    @CsvBindByPosition(position = 0)
    private String itemName;
    @CsvBindByPosition(position = 1)
    private String itemId;
    @CsvBindByPosition(position = 2)
    private int itemGPValue;
}
