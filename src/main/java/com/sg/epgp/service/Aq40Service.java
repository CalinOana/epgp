package com.sg.epgp.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.sg.epgp.model.PlayerEpGp;
import com.sg.epgp.model.RaidAttendance;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigInteger.ZERO;
import static java.util.stream.Collectors.*;

@Service
public class Aq40Service {
    private static final String COMMA_DELIMITER = ";";
    private final AltService altService;

    public Aq40Service(AltService altService) {
        this.altService = altService;
    }

    public List<RaidAttendance> readAttendance() throws IOException {
        List<RaidAttendance> beans = new CsvToBeanBuilder(new FileReader("aq40attendance.csv"))
            .withType(RaidAttendance.class).build().parse();
        return beans;
    }

    public List<PlayerEpGp> getAq40EpGp(int totalRaidEP, BigDecimal decay) throws IOException {
        final List<RaidAttendance> raidAttendances = readAttendance();
        final Map<String, Set<String>> alts = altService.readGuildMembers(false);
        return calculateEpGp(totalRaidEP, decay, raidAttendances, alts);
    }

    public List<PlayerEpGp> calculateEpGp(int totalRaidEP, BigDecimal decay, List<RaidAttendance> raidAttendances, Map<String, Set<String>> alts) {
        List<PlayerEpGp> playerEpGpList = new ArrayList<>();
        raidAttendances.forEach(raidAttendance -> {
            final String name = raidAttendance.getName();
            final PlayerEpGp playerEpGp = computeEPGpForPlayer(raidAttendance, totalRaidEP, decay);
            if (!playerEpGp.getEffortPoints().equals(BigDecimal.ZERO))
                alts.entrySet().stream().filter(playerHasAlts(name)).findFirst().ifPresentOrElse(
                    playerCharacters -> {
                        final List<PlayerEpGp> clonedEpForAllPlayerChars = cloneEpForAllPlayerChars(playerEpGp, playerCharacters);
                        final boolean nameOrAltNameFoundInList = playerEpGpList.stream().anyMatch(playerEpGp1 -> serverlessNameEquals(playerEpGp1.getName(), name));
                        if (!nameOrAltNameFoundInList) {
                            playerEpGpList.addAll(clonedEpForAllPlayerChars);
                        } else {

                            final Map<String, PlayerEpGp> collect = Stream.concat(clonedEpForAllPlayerChars.stream(), playerEpGpList.stream())
                                .collect(
                                    groupingBy(
                                        PlayerEpGp::getName,
                                        reducing(
                                            new PlayerEpGp("", BigDecimal.ZERO, BigDecimal.ZERO)
                                            , (playerEpGp1, playerEpGp2) -> new PlayerEpGp(playerEpGp2.getName(), playerEpGp1.getEffortPoints().add(playerEpGp2.getEffortPoints()), playerEpGp1.getGearPoints().add(playerEpGp2.getGearPoints())))));
                            playerEpGpList.removeIf(playerEpGp1 -> collect.containsKey(playerEpGp1.getName()));
                            playerEpGpList.addAll(collect.values());
                        }
                    },
                    () -> playerEpGpList.add(playerEpGp));
        });
        return playerEpGpList;
    }

    private static Predicate<Map.Entry<String, Set<String>>> playerHasAlts(String name) {
        return (Map.Entry<String, Set<String>> mainAltMapping) ->
            serverlessNameEquals(name, mainAltMapping.getKey()) ||
                mainAltMapping.getValue().stream().anyMatch(alt -> serverlessNameEquals(name, alt));
    }

    private static boolean serverlessNameEquals(String nameWithNoServer, String nameContainingServer) {
        final boolean equals = stripServerName(nameContainingServer).equals(stripServerName(nameWithNoServer));
        return equals;
    }

    private static String stripServerName(String nameContainingServer) {
        return ("" + nameContainingServer).replaceAll("-PyrewoodVillage", "").replaceAll("-MirageRaceway", "").replaceAll("-NethergardeKeep", "");
    }

    private List<PlayerEpGp> cloneEpForAllPlayerChars(PlayerEpGp epForPlayer, Map.Entry<String, Set<String>> playerCharacters) {
        Set<String> allPlayerCharacters = playerCharacters.getValue();
        allPlayerCharacters.add(playerCharacters.getKey());
        return allPlayerCharacters.stream().map(o -> epForPlayer.toBuilder().name(String.valueOf(o)).build())
            .collect(Collectors.toList());
    }

    private PlayerEpGp computeEPGpForPlayer(RaidAttendance raidAttendance, int totalRaidEP, BigDecimal decay) {
        final BigDecimal epForPlayer = computeEpForPlayer(raidAttendance, totalRaidEP, decay);
        return PlayerEpGp.builder()
            .name(raidAttendance.getName())
            .effortPoints(epForPlayer)
            .gearPoints(computeGpForPlayer(epForPlayer))
            .build();
    }

    private BigDecimal computeGpForPlayer(BigDecimal epForPlayer) {
        return epForPlayer.multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal computeEpForPlayer(RaidAttendance raidAttendance, int totalRaidEP, BigDecimal decay) {
        BigDecimal ep = BigDecimal.ZERO;
        for (Map.Entry<Integer, Boolean> integerBooleanEntry : raidAttendance.getAttendance().entries()) {
            int raidNumber = integerBooleanEntry.getKey() - 2;
            BigDecimal computedEP = BigDecimal.valueOf(totalRaidEP);
            if (null != integerBooleanEntry.getValue() && integerBooleanEntry.getValue()) {
                for (int i = 0; i < raidNumber; i++) {
                    computedEP = computedEP.multiply(decay);
                }
                ep = ep.add(computedEP);
            }
        }
        return ep.setScale(0, RoundingMode.HALF_UP);
    }
}
