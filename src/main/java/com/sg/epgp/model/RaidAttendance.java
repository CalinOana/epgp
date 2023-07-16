package com.sg.epgp.model;

import com.opencsv.bean.CsvBindAndJoinByPosition;
import com.opencsv.bean.CsvBindByPosition;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.MultiValuedMap;
@Data
@Builder
public class RaidAttendance {
    @CsvBindByPosition(position = 0)
    private String name;
    @CsvBindAndJoinByPosition(position = "2-14", elementType = Boolean.class)
    private MultiValuedMap<Integer, Boolean> attendance;
}
