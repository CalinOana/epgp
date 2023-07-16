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

import static java.util.stream.Collectors.*;

@Service
public class Aq40Service {
    private final AltService altService;

    private final EpgpComputingService epgpComputingService;

    public Aq40Service(AltService altService, EpgpComputingService epgpComputingService) {
        this.altService = altService;
        this.epgpComputingService = epgpComputingService;
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
        return epgpComputingService.getPlayerEpGps(totalRaidEP, decay, raidAttendances, alts);
    }
}
