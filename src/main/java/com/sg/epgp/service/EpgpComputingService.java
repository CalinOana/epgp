package com.sg.epgp.service;

import com.sg.epgp.model.PlayerEpGp;
import com.sg.epgp.model.RaidAttendance;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

@Service
public class EpgpComputingService {
    public List<PlayerEpGp> getPlayerEpGps(int totalRaidEP, BigDecimal decay, List<RaidAttendance> raidAttendances, Map<String, Set<String>> alts) {
        final List<PlayerEpGp> playerEpGpList = new ArrayList<>();
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
                            combineWithEpGpForPlayerAndAlts(playerEpGpList, clonedEpForAllPlayerChars);
                        }
                    },
                    () -> playerEpGpList.add(playerEpGp));
        });
        return playerEpGpList;
    }

    public List<PlayerEpGp> combineWithEpGpForPlayerAndAlts(List<PlayerEpGp> playerEpGpList, List<PlayerEpGp> clonedEpForAllPlayerChars) {
        final Map<String, PlayerEpGp> collect = Stream.concat(clonedEpForAllPlayerChars.stream(), playerEpGpList.stream())
            .collect(
                groupingBy(
                    PlayerEpGp::getName,
                    reducing(
                        new PlayerEpGp("", BigDecimal.ZERO, BigDecimal.ZERO)
                        , (playerEpGp1, playerEpGp2) -> new PlayerEpGp(playerEpGp2.getName(), playerEpGp1.getEffortPoints().add(playerEpGp2.getEffortPoints()), playerEpGp1.getGearPoints().add(playerEpGp2.getGearPoints())))));
        playerEpGpList.removeIf(playerEpGp1 -> collect.containsKey(playerEpGp1.getName()));
        playerEpGpList.addAll(collect.values());
        return playerEpGpList;
    }
    public List<PlayerEpGp> combineWithExistingEpGpForPlayer(List<PlayerEpGp> playerEpGpList, List<PlayerEpGp> clonedEpForAllPlayerChars) {
        final Map<String, PlayerEpGp> collect = Stream.concat(playerEpGpList.stream(), clonedEpForAllPlayerChars.stream())
            .collect(
                groupingBy(
                    PlayerEpGp::getName,
                    reducing(
                        new PlayerEpGp("", BigDecimal.ZERO, BigDecimal.ZERO)
                        , (playerEpGp1, playerEpGp2) -> new PlayerEpGp(playerEpGp2.getName(), playerEpGp1.getEffortPoints().add(playerEpGp2.getEffortPoints()), playerEpGp1.getGearPoints().add(playerEpGp2.getGearPoints())))));
        return collect.values().stream().collect(Collectors.toList());
    }

    List<PlayerEpGp> cloneEpForAllPlayerChars(PlayerEpGp epForPlayer, Map.Entry<String, Set<String>> playerCharacters) {
        Set<String> allPlayerCharacters = playerCharacters.getValue();
        allPlayerCharacters.add(playerCharacters.getKey());
        return allPlayerCharacters.stream().map(o -> epForPlayer.toBuilder().name(String.valueOf(o)).build())
            .collect(Collectors.toList());
    }

    private PlayerEpGp computeEPGpForPlayer(RaidAttendance raidAttendance, int totalRaidEP, BigDecimal decay) {
        final BigDecimal epForPlayer = computeEpForPlayer(raidAttendance, totalRaidEP, decay);
        return PlayerEpGp.builder()
            .name(raidAttendance.getName())
            .effortPoints(epForPlayer
            )
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


    static Predicate<Map.Entry<String, Set<String>>> playerHasAlts(String name) {
        return (Map.Entry<String, Set<String>> mainAltMapping) ->
            serverlessNameEquals(name, mainAltMapping.getKey()) ||
                mainAltMapping.getValue().stream().anyMatch(alt -> serverlessNameEquals(name, alt));
    }

    static boolean serverlessNameEquals(String nameWithNoServer, String nameContainingServer) {
        final boolean equals = stripServerName(nameContainingServer).equals(stripServerName(nameWithNoServer));
        return equals;
    }


    private static String stripServerName(String nameContainingServer) {
        return ("" + nameContainingServer).replaceAll("-PyrewoodVillage", "").replaceAll("-MirageRaceway", "").replaceAll("-NethergardeKeep", "");
    }

    public List<PlayerEpGp> combineWithExistingEpGpForPlayerAndAlts(List<PlayerEpGp> aq40EpGp, List<PlayerEpGp> naxxEpGp) {
        return combineWithExistingEpGpForPlayer(aq40EpGp, naxxEpGp);
    }
}
