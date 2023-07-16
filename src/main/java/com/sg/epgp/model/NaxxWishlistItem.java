package com.sg.epgp.model;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class NaxxWishlistItem {
    @CsvBindByPosition(position = 3)
    private String characterName;
    @CsvBindByPosition(position = 9)
    private String itemName;
    @CsvBindByPosition(position = 10)
    private String itemId;
    @CsvBindByPosition(position = 13)
    @CsvDate("yyyy-MM-dd' 'HH:mm:ss")
    private Date receivedAt;
}
