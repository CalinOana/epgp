package com.sg.epgp.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.sg.epgp.model.PlayerEpGp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.*;

class PlayerEpGpWriterServiceTest {

    PlayerEpGpWriterService playerEpGpWriterService;

    @BeforeEach
    void setup() {
        playerEpGpWriterService = new PlayerEpGpWriterService();

    }

    @Test
    void writePlayerEpGpToCsv() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        final List<PlayerEpGp> list = Arrays.asList(
            PlayerEpGp.builder().name("Liabala-PyrewoodVillage").effortPoints(BigDecimal.ZERO).gearPoints(TEN).build(),
            PlayerEpGp.builder().name("NubSlaberg-NethergardeKeep").effortPoints(TEN).gearPoints(TEN).build()
        );

        playerEpGpWriterService.writePlayerEpGpToCsv(list,"export.csv");
    }
}
