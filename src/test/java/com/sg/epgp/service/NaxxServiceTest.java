package com.sg.epgp.service;

import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import com.sg.epgp.model.ItemGP;
import com.sg.epgp.model.NaxxWishlistItem;
import com.sg.epgp.model.PlayerEpGp;
import com.sg.epgp.model.RaidAttendance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NaxxServiceTest {
    NaxxService naxxService;
    ServerPerspectiveService serverPerspectiveService;
    @BeforeEach
    void setup() {
        serverPerspectiveService = new ServerPerspectiveService();
        naxxService = new NaxxService(new AltService(serverPerspectiveService), new EpgpComputingService());
    }

    @Test
    void readAttendance() throws IOException {
        final List<RaidAttendance> raidAttendances = naxxService.readAttendance();
        assertNotNull(raidAttendances);
    }

    @Test
    void getNaxxEpGp() throws IOException {
        final List<PlayerEpGp> naxxEpGp = naxxService.getNaxxEpGp(198, BigDecimal.valueOf(0.8));
        assertNotNull(naxxEpGp);
        assertNotEquals(0, naxxEpGp.size());
    }

    @Test
    void readWishlistItemsReceived() throws IOException {
        final List<NaxxWishlistItem> naxxWishlistItems = naxxService.readWishlistItemsReceived();
        assertNotNull(naxxWishlistItems);
        assertNotEquals(0, naxxWishlistItems.size());
    }

    @Test
    void readGP() throws IOException {
        final List<ItemGP> itemGPS = naxxService.readGP();
        assertNotNull(itemGPS);
        assertNotEquals(0, itemGPS.size());
    }

    @Test
    void testGetNaxxEpGp() throws IOException {
        final List<PlayerEpGp> naxxEpGp = naxxService.getNaxxEpGp(198, BigDecimal.valueOf(0.8));
        assertNotNull(naxxEpGp);
        assertNotEquals(0, naxxEpGp.size());

    }
}
