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
            int decayedValueOfGpItem = epgpComputingService.computeDecayedValueOfGpItem(naxxWishlistItem, decay, naxxGearPoints);
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



    public List<PlayerEpGp> calculateEpGp(int totalRaidEP, BigDecimal decay, List<RaidAttendance> raidAttendances, Map<String, Set<String>> alts) {
        return epgpComputingService.getPlayerEpGps(totalRaidEP, decay, raidAttendances, alts);
    }

}
