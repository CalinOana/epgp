package com.sg.epgp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AltServiceTest {

    AltService altService;

    ServerPerspectiveService serverPerspectiveService;

    @BeforeEach
    void setUp() {
        serverPerspectiveService = new ServerPerspectiveService();
        altService = new AltService(serverPerspectiveService);
    }

    @Test
    @DisplayName("Given valid header assert readGuildMembers returns correct list of alts")
    void readAlts() throws IOException {
        final Map<String, Set<String>> stringSetMap = altService.readGuildMembers(true);
        assertNotNull(stringSetMap);
    }
}
