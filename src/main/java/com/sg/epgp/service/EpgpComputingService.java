package com.sg.epgp.service;

import com.sg.epgp.model.ItemGP;
import com.sg.epgp.model.NaxxWishlistItem;
import com.sg.epgp.model.PlayerEpGp;
import com.sg.epgp.model.RaidAttendance;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

@Service
public class EpgpComputingService {

    private static final HashMap<Integer, Integer> T3_ITEM_TO_TOKEN;

    static {
        T3_ITEM_TO_TOKEN = new HashMap<>();
        //pala
        T3_ITEM_TO_TOKEN.put(22428, 22360);
        T3_ITEM_TO_TOKEN.put(22429, 22361);
        T3_ITEM_TO_TOKEN.put(22425, 22350);
        T3_ITEM_TO_TOKEN.put(22424, 22362);
        T3_ITEM_TO_TOKEN.put(22426, 22364);
        T3_ITEM_TO_TOKEN.put(22431, 22363);
        T3_ITEM_TO_TOKEN.put(22427, 22359);
        T3_ITEM_TO_TOKEN.put(22430, 22365);
        //priest
        T3_ITEM_TO_TOKEN.put(22514, 22367);
        T3_ITEM_TO_TOKEN.put(22515, 22368);
        T3_ITEM_TO_TOKEN.put(22512, 22351);
        T3_ITEM_TO_TOKEN.put(22519, 22369);
        T3_ITEM_TO_TOKEN.put(22517, 22371);
        T3_ITEM_TO_TOKEN.put(22518, 22370);
        T3_ITEM_TO_TOKEN.put(22513, 22366);
        T3_ITEM_TO_TOKEN.put(22516, 22372);
        //druid
        T3_ITEM_TO_TOKEN.put(22490, 22360);
        T3_ITEM_TO_TOKEN.put(22491, 22361);
        T3_ITEM_TO_TOKEN.put(22488, 22350);
        T3_ITEM_TO_TOKEN.put(22495, 22362);
        T3_ITEM_TO_TOKEN.put(22493, 22364);
        T3_ITEM_TO_TOKEN.put(22494, 22363);
        T3_ITEM_TO_TOKEN.put(22489, 22359);
        T3_ITEM_TO_TOKEN.put(22492, 22365);
        //warrior
        T3_ITEM_TO_TOKEN.put(22418, 22353);
        T3_ITEM_TO_TOKEN.put(22419, 22354);
        T3_ITEM_TO_TOKEN.put(22416, 22349);
        T3_ITEM_TO_TOKEN.put(22423, 22355);
        T3_ITEM_TO_TOKEN.put(22421, 22357);
        T3_ITEM_TO_TOKEN.put(22422, 22356);
        T3_ITEM_TO_TOKEN.put(22417, 22352);
        T3_ITEM_TO_TOKEN.put(22420, 22358);
        //hunter
        T3_ITEM_TO_TOKEN.put(22438, 22360);
        T3_ITEM_TO_TOKEN.put(22439, 22361);
        T3_ITEM_TO_TOKEN.put(22436, 22350);
        T3_ITEM_TO_TOKEN.put(22443, 22362);
        T3_ITEM_TO_TOKEN.put(22441, 22364);
        T3_ITEM_TO_TOKEN.put(22442, 22363);
        T3_ITEM_TO_TOKEN.put(22437, 22359);
        T3_ITEM_TO_TOKEN.put(22440, 22365);
        //warlock
        T3_ITEM_TO_TOKEN.put(22506, 22367);
        T3_ITEM_TO_TOKEN.put(22507, 22368);
        T3_ITEM_TO_TOKEN.put(22504, 22351);
        T3_ITEM_TO_TOKEN.put(22511, 22369);
        T3_ITEM_TO_TOKEN.put(22509, 22371);
        T3_ITEM_TO_TOKEN.put(22510, 22370);
        T3_ITEM_TO_TOKEN.put(22505, 22366);
        T3_ITEM_TO_TOKEN.put(22508, 22372);
        //Mage
        T3_ITEM_TO_TOKEN.put(22498, 22367);
        T3_ITEM_TO_TOKEN.put(22499, 22368);
        T3_ITEM_TO_TOKEN.put(22496, 22351);
        T3_ITEM_TO_TOKEN.put(22503, 22369);
        T3_ITEM_TO_TOKEN.put(22501, 22371);
        T3_ITEM_TO_TOKEN.put(22502, 22370);
        T3_ITEM_TO_TOKEN.put(22497, 22366);
        T3_ITEM_TO_TOKEN.put(22500, 22372);
        //Rogue
        T3_ITEM_TO_TOKEN.put(22478, 22353);
        T3_ITEM_TO_TOKEN.put(22479, 22354);
        T3_ITEM_TO_TOKEN.put(22476, 22349);
        T3_ITEM_TO_TOKEN.put(22483, 22355);
        T3_ITEM_TO_TOKEN.put(22481, 22357);
        T3_ITEM_TO_TOKEN.put(22482, 22356);
        T3_ITEM_TO_TOKEN.put(22477, 22352);
        T3_ITEM_TO_TOKEN.put(22480, 22358);
    }
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

    public int computeDecayedValueOfGpItem(NaxxWishlistItem naxxWishlistItem, BigDecimal decay, List<ItemGP> naxxGearPoints) {
        final ItemGP itemGP = naxxGearPoints.stream().filter(item -> {
            return item.getItemId().equals(naxxWishlistItem.getItemId()) || item.getItemId().equals(String.valueOf(T3_ITEM_TO_TOKEN.get(Integer.parseInt(naxxWishlistItem.getItemId()))));
        }).findFirst().get();
        LocalDate localDate = new java.sql.Date(naxxWishlistItem.getReceivedAt().getTime()).toLocalDate();
        LocalDate now = LocalDate.now();
        LocalDate oneWeekAgo = now.minusWeeks(1);
        int decayedGpValueOfItem = itemGP.getItemGPValue();
        for (int i = 0; i < 13; i++) {
            decayedGpValueOfItem = (int) (decayedGpValueOfItem * decay.intValue());
            if (localDate.isBefore(now) && localDate.isAfter(oneWeekAgo)) {
                return decayedGpValueOfItem;
            }
            now = now.minusWeeks(1);
            oneWeekAgo = oneWeekAgo.minusWeeks(1);
        }
        return 0;
    }
}
