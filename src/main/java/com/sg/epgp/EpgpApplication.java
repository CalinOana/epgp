package com.sg.epgp;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.sg.epgp.model.PlayerEpGp;
import com.sg.epgp.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootApplication(scanBasePackages = "com.sg.epgp.*")
public class EpgpApplication {
    @Autowired
    public NaxxService naxxService;
    @Autowired
    public Aq40Service aq40Service;
    @Autowired
    public AltService altService;
    @Autowired
    public ServerPerspectiveService serverPerspectiveService;
    @Autowired
    public EpgpComputingService epgpComputingService;
    @Autowired
    public static BigDecimal DECAY = BigDecimal.valueOf(0.8);
    @Autowired
    public PlayerEpGpWriterService playerEpGpWriterService;

    public static void main(String[] args) {
        SpringApplication.run(EpgpApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        final List<PlayerEpGp> aq40EpGp = aq40Service.getAq40EpGp(82, DECAY);
        final List<PlayerEpGp> naxxEpGp = naxxService.getNaxxEpGp(198, BigDecimal.valueOf(0.8));
        final List<PlayerEpGp> combinedNaxxAndAq40EpGp = epgpComputingService.combineWithExistingEpGpForPlayerAndAlts(aq40EpGp, naxxEpGp);
        final List<PlayerEpGp> serverPerspectiveCombinedNaxxAndAq40EpGP = serverPerspectiveService.getServerPerspectiveEpGP(combinedNaxxAndAq40EpGp);
        altService.readGuildMembers(true);
        playerEpGpWriterService.writePlayerEpGpToCsv(serverPerspectiveCombinedNaxxAndAq40EpGP, "epGpStandings.csv");
        System.out.println("hello world, I have just started up");
    }

}
