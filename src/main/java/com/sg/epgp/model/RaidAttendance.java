package com.sg.epgp.model;

import com.opencsv.bean.CsvBindAndJoinByPosition;
import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MultiValuedMap;
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class RaidAttendance {
    @CsvBindByPosition(position = 0)
    private String name;
    @CsvBindAndJoinByPosition(position = "2-14", elementType = Boolean.class)
    private MultiValuedMap<Integer, Boolean> attendance;
}
