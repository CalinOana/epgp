package com.sg.epgp.service;

import com.sg.epgp.model.PlayerEpGp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.*;

class ServerPerspectiveServiceTest {

    ServerPerspectiveService serverPerspectiveService;

    @BeforeEach
    void setUp() {
        serverPerspectiveService = new ServerPerspectiveService();
    }

    @Test
    @DisplayName("Given PlayerEpGp of players when getServerPerspectiveEpGp assert each player is duplicated as perspective of server he is on (without -<ServerName>) AND perspective of other servers (with -<ServerName>) ")
    void getServerPerspectiveEpGP() {
        final List<PlayerEpGp> list = Arrays.asList(
            PlayerEpGp.builder().name("Liabala-PyrewoodVillage").effortPoints(BigDecimal.ZERO).gearPoints(TEN).build(),
            PlayerEpGp.builder().name("NubSlaberg-NethergardeKeep").effortPoints(TEN).gearPoints(TEN).build()
        );

        final List<PlayerEpGp> serverPerspectiveEpGP = serverPerspectiveService.getServerPerspectiveEpGP(list);
        assertNotNull(serverPerspectiveEpGP);
        assertEquals(list.size() * 2, serverPerspectiveEpGP.size());
        assertTrue(serverPerspectiveEpGP.stream().anyMatch(playerEpGp ->
            playerEpGp.getName().equals("Liabala") &&
                playerEpGp.getGearPoints().equals(TEN) &&
                playerEpGp.getEffortPoints().equals(BigDecimal.ZERO)));
        assertTrue(serverPerspectiveEpGP.stream().anyMatch(playerEpGp ->
            playerEpGp.getName().equals("Liabala-PyrewoodVillage") &&
                playerEpGp.getGearPoints().equals(TEN) &&
                playerEpGp.getEffortPoints().equals(BigDecimal.ZERO)));
        assertTrue(serverPerspectiveEpGP.stream().anyMatch(playerEpGp ->
            playerEpGp.getName().equals("NubSlaberg") &&
                playerEpGp.getGearPoints().equals(BigDecimal.TEN) &&
                playerEpGp.getEffortPoints().equals(TEN)));
        assertTrue(serverPerspectiveEpGP.stream().anyMatch(playerEpGp ->
            playerEpGp.getName().equals("NubSlaberg-NethergardeKeep") &&
                playerEpGp.getGearPoints().equals(BigDecimal.TEN) &&
                playerEpGp.getEffortPoints().equals(TEN)));
    }
}
