package com.sg.epgp.service;

import com.sg.epgp.model.PlayerEpGp;
import com.sg.epgp.model.RaidAttendance;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class Aq40ServiceTest {
    Aq40Service aq40Service;

    @BeforeEach
    void setUp() {
        aq40Service = new Aq40Service(new AltService());
    }

    @Test
    void readAttendance() throws IOException {
        final List<RaidAttendance> raidAttendances = aq40Service.readAttendance();
        assertNotNull(raidAttendances);
    }

    @Test
    void getAq40EpGpTest() throws IOException {
        final List<PlayerEpGp> playerEpGpList = aq40Service.getAq40EpGp(82, BigDecimal.valueOf(0.8));
        assertNotNull(playerEpGpList);
    }

    @Test
    @DisplayName("Given EpGp of 2 alts of playyer when calculating EPGP assume effort points and gear points are calculated correctly")
    void calculateEpGpTest() {

        final HashMap<String, Set<String>> alts = new HashMap<>();
        alts.put("John-PyrewoodVillage", new HashSet<>(Arrays.asList("Doe-NethergardeKeep", "Ben-MirageRaceway", "Dover-PyrewoodVillage")));
        final ArrayListValuedHashMap<Integer, Boolean> johnAttendance = new ArrayListValuedHashMap<>();
        johnAttendance.put(2, true);
        johnAttendance.put(3, true);
        johnAttendance.put(4, false);
        johnAttendance.put(5, false);
        final ArrayListValuedHashMap<Integer, Boolean> doeAttendance = new ArrayListValuedHashMap<>();
        doeAttendance.put(2, true);
        doeAttendance.put(3, false);
        doeAttendance.put(4, false);
        doeAttendance.put(5, false);

        final List<RaidAttendance> raidAttendanceList = Arrays.asList(RaidAttendance.builder().name("John")
            .attendance(johnAttendance).build(), RaidAttendance.builder().name("Doe").attendance(doeAttendance).build());
        final List<PlayerEpGp> playerEpGpList = aq40Service.calculateEpGp(82, BigDecimal.valueOf(0.8), raidAttendanceList, alts);
        assertNotNull(playerEpGpList);
        assertEquals(new HashSet<>(playerEpGpList).size(), playerEpGpList.size());
        final PlayerEpGp johnEpGp = playerEpGpList.stream().filter(playerEpGp -> "John-PyrewoodVillage".equals(playerEpGp.getName())).findFirst().get();
        assertEquals(BigDecimal.valueOf(82 * 2 + 66), johnEpGp.getEffortPoints());
        assertEquals(BigDecimal.valueOf((long) ((82 * 2 + 66) * 0.1), 0), johnEpGp.getGearPoints());
    }
}
