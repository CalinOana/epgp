package com.sg.epgp;

import com.sg.epgp.model.PlayerEpGp;
import com.sg.epgp.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class FullEpGpIntegrationTest {
    public NaxxService naxxService;
    public Aq40Service aq40Service;
    public AltService altService;

    public ServerPerspectiveService serverPerspectiveService;

    public EpgpComputingService epgpComputingService;
    public static BigDecimal DECAY = BigDecimal.valueOf(0.8);

    @BeforeEach
    void setUp() {
        epgpComputingService = new EpgpComputingService();
        altService = new AltService(serverPerspectiveService);
        naxxService = new NaxxService(altService, epgpComputingService);
        aq40Service = new Aq40Service(altService, epgpComputingService);
    }

    @Test
    @DisplayName("Given Aq40 and Naxx EpGpList with regular imported CSVs assert naxxService computes correct addition of AQ40 EP and GP")
    void test() throws IOException {
        final List<PlayerEpGp> aq40EpGp = aq40Service.getAq40EpGp(82, DECAY);
        assertNotNull(aq40EpGp);
        assertNotEquals(0, aq40EpGp.size());
        final List<PlayerEpGp> naxxEpGp = naxxService.getNaxxEpGp(198, BigDecimal.valueOf(0.8));
        assertNotNull(naxxEpGp);
        assertNotEquals(0, naxxEpGp.size());
        final Map<String, Set<String>> playerCharacters = altService.readGuildMembers(false);
        assertNotNull(playerCharacters);
        assertNotEquals(0, playerCharacters.size());
        final List<PlayerEpGp> combinedNaxxAndAq40EpGp = epgpComputingService.combineWithExistingEpGpForPlayerAndAlts(aq40EpGp, naxxEpGp);
        assertNotNull(combinedNaxxAndAq40EpGp);
        assertNotEquals(0, combinedNaxxAndAq40EpGp.size());
        playerCharacters.forEach((String character, Set<String> alts) -> {
            final Optional<PlayerEpGp> altOrMain = combinedNaxxAndAq40EpGp.stream().filter(playerEpGp -> filterPlayerEpGpByCharacterAndALts(character, alts, playerEpGp)).findAny();
            if(altOrMain.isPresent()){
                final PlayerEpGp playerEpGp1 = altOrMain.get();
                Stream.of(character, alts).forEach(o -> {
                    combinedNaxxAndAq40EpGp.stream().filter(playerEpGp -> playerEpGp.getName().equals(character)).forEach(playerEpGp -> {
                        assertEquals(playerEpGp1.getGearPoints(),playerEpGp.getGearPoints());
                        assertEquals(playerEpGp1.getEffortPoints(),playerEpGp.getEffortPoints());
                    });
                });
            }
        });
    }

    private static boolean filterPlayerEpGpByCharacterAndALts(String character, Set<String> alts, PlayerEpGp playerEpGp) {
        final boolean contains = alts.contains(playerEpGp.getName());
        final boolean equals = playerEpGp.getName().equals(character);
        return equals || contains;
    }
}
