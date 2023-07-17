package com.sg.epgp.service;

import com.sg.epgp.model.PlayerEpGp;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
public class ServerPerspectiveService {
    static final List<String> SERVER_CLUSTER_SERVERS = Arrays.asList("PyrewoodVillage", "NethergardeKeep", "MirageRaceway");

    public List<PlayerEpGp> getServerPerspectiveEpGP(List<PlayerEpGp> playerEpGpList) {
        List<PlayerEpGp> toReturn = new ArrayList<>();
        SERVER_CLUSTER_SERVERS.forEach(server ->
            playerEpGpList.forEach(playerEpGp -> {
                if (playerEpGp.getName().contains(server) && toReturn.stream().noneMatch(playerEpGp1 -> playerEpGp1.getName().equals(playerEpGp))) {
                    toReturn.add(playerEpGp.toBuilder().name(playerEpGp.getName().replaceAll("-" + server, "")).build());
                } else {
                    final Optional<PlayerEpGp> first = toReturn.stream().filter(playerEpGp1 -> playerEpGp1.getName().equals(playerEpGp.getName())).findFirst();
                    if (first.isEmpty()) {
                        toReturn.add(playerEpGp);
                    }
                }
            }));
        return toReturn;
    }

    public static void printMap(Map<String, Set<String>> map) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("exports\\guildMainAltMapping.txt"));
        Arrays.asList("PyrewoodVillage", "NethergardeKeep", "MirageRaceway").forEach(
            s -> {
                map.forEach((String main, Set<String> alts) -> {
                    try {
                        writer.write("[\"" + main.replace("-" + s, "") + "\"] = {");
                        writer.newLine();
                        alts.forEach(alt -> {
                            try {
                                writer.write("\t\"" + alt.replace("-" + s, "") + "\",");
                                writer.newLine();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        writer.write("},");
                        writer.newLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        );
        writer.close();
    }
}
