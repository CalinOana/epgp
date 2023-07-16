package com.sg.epgp.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Service
public class AltService {
    private static final String COMMA_DELIMITER = ";";
    private final ServerPerspectiveService serverPerspectiveService;

    public AltService(ServerPerspectiveService serverPerspectiveService) {
        this.serverPerspectiveService = serverPerspectiveService;
    }

    public Map<String, Set<String>> readGuildMembers(boolean onlyAlts) throws IOException {
        List<List<String>> recordsFromFile = getRecordsFromFile();
        Map<String, Set<String>> mainsAndAlts = mapAndRemoveDuplicates(recordsFromFile, onlyAlts);
        if (onlyAlts) {
            ServerPerspectiveService.printMap(mainsAndAlts);
        }
        return mainsAndAlts;
    }



    private static Map<String, Set<String>> mapAndRemoveDuplicates(List<List<String>> recordsFromFile, boolean onlyAlts) {
        Map<String, Set<String>> map = new HashMap<>();

        recordsFromFile.stream().filter(tokens -> !onlyAlts || tokens.size() == 3 && Arrays.asList("Main", "Alt").contains(tokens.get(1)))
            .forEach((List<String> mainOrAlts) -> {

                boolean foundDuplicate = false;

                for (Map.Entry<String, Set<String>> entry : map.entrySet()) {

                    String main = entry.getKey();
                    Set<String> alts = entry.getValue();
                    List<String> tokenizedMainOrAltsFromFile = mainOrAlts.stream().flatMap(s -> Arrays.stream(s.split(","))).toList();
                    foundDuplicate = tokenizedMainOrAltsFromFile.contains(main) || tokenizedMainOrAltsFromFile.stream().anyMatch(alts::contains);
                    if (foundDuplicate) {
                        break;
                    }
                }
                if (!foundDuplicate) {
                    map.put(mainOrAlts.get(0), mainOrAlts.size() == 3 ?
                        new HashSet<>(List.of(mainOrAlts.get(2).split(","))) :
                        new HashSet<>(Collections.emptySet()));
                }
            });
        return map;
    }

    private static List<List<String>> getRecordsFromFile() throws IOException {
        List<List<String>> recordsFromFile = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("alts.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("(main)", "");
                String[] values = line.split(COMMA_DELIMITER);
                recordsFromFile.add(Arrays.asList(values));
            }
        }
        return recordsFromFile;
    }
}
