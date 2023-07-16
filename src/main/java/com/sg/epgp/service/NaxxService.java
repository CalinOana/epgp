package com.sg.epgp.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.sg.epgp.model.ItemGP;
import com.sg.epgp.model.NaxxWishlistItem;
import com.sg.epgp.model.PlayerEpGp;
import com.sg.epgp.model.RaidAttendance;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sg.epgp.service.EpgpComputingService.playerHasAlts;
import static com.sg.epgp.service.EpgpComputingService.serverlessNameEquals;
import static java.math.BigDecimal.ZERO;

@Service
public class NaxxService {
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

    EpgpComputingService epgpComputingService;
    private final AltService altService;

    public NaxxService(AltService altService, EpgpComputingService epgpComputingService) {
        this.altService = altService;
        this.epgpComputingService = epgpComputingService;
    }

    public List<RaidAttendance> readAttendance() throws IOException {
        List<RaidAttendance> beans = new CsvToBeanBuilder(new FileReader("naxxattendance.csv"))
            .withType(RaidAttendance.class).build().parse();
        return beans;
    }

    public List<NaxxWishlistItem> readWishlistItemsReceived() throws IOException {
        List<NaxxWishlistItem> naxxWishlistItems = new CsvToBeanBuilder(new FileReader("naxxWishlistItemsReceived.csv"))
            .withType(NaxxWishlistItem.class).build().parse();
        naxxWishlistItems.removeIf(naxxWishlistItem -> null == naxxWishlistItem.getReceivedAt());
        return naxxWishlistItems;
    }

    public List<ItemGP> readGP() throws IOException {
        List<ItemGP> itemGps = new CsvToBeanBuilder(new FileReader("naxxGPValues.csv"))
            .withType(ItemGP.class).withSeparator(';').build().parse();
        return itemGps;
    }

    public List<PlayerEpGp> getNaxxEpGp(int totalRaidEP, BigDecimal decay) throws IOException {
        final List<RaidAttendance> raidAttendances = readAttendance();
        final Map<String, Set<String>> alts = altService.readGuildMembers(false);
        List<PlayerEpGp> playerEpGpList = calculateEpGp(totalRaidEP, decay, raidAttendances, alts);
        final List<NaxxWishlistItem> naxxWishlistItems = readWishlistItemsReceived();
        final List<ItemGP> naxxGearPoints = readGP();
        naxxWishlistItems.forEach(naxxWishlistItem -> {
            int decayedValueOfGpItem = computeDecayedValueOfGpItem(naxxWishlistItem, decay, naxxGearPoints);
            if (decayedValueOfGpItem != 0) {
                alts.entrySet().stream().filter(playerHasAlts(naxxWishlistItem.getCharacterName())).findFirst().ifPresentOrElse(
                    playerCharacters -> {
                        final List<PlayerEpGp> clonedEpForAllPlayerChars = epgpComputingService.cloneEpForAllPlayerChars(new PlayerEpGp(naxxWishlistItem.getCharacterName(), ZERO, BigDecimal.valueOf(decayedValueOfGpItem)), playerCharacters);
                        final boolean nameOrAltNameFoundInList = playerEpGpList.stream().anyMatch(playerEpGp1 -> serverlessNameEquals(playerEpGp1.getName(), naxxWishlistItem.getCharacterName()));
                        if (!nameOrAltNameFoundInList) {
                            playerEpGpList.addAll(clonedEpForAllPlayerChars);
                        } else {
                            epgpComputingService.combineWithExistingEpGpForPlayerAndAlts(playerEpGpList, clonedEpForAllPlayerChars);
                        }
                    },
                    () -> {
                    });
            }
        });
        return playerEpGpList;
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

    public List<PlayerEpGp> calculateEpGp(int totalRaidEP, BigDecimal decay, List<RaidAttendance> raidAttendances, Map<String, Set<String>> alts) {
        return epgpComputingService.getPlayerEpGps(totalRaidEP, decay, raidAttendances, alts);
    }

}
