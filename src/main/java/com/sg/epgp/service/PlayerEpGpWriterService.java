package com.sg.epgp.service;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.bean.comparator.LiteralComparator;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.sg.epgp.model.PlayerEpGp;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

@Service
public class PlayerEpGpWriterService {
    public void writePlayerEpGpToCsv(List<PlayerEpGp> playerEpGpList, String fileName) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        Writer writer = new FileWriter(fileName);
        HeaderColumnNameMappingStrategy strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(PlayerEpGp.class);
        strategy.setColumnOrderOnWrite(new LiteralComparator<>(Arrays.asList("NAME", "EFFORTPOINTS", "GEARPOINTS").toArray(String[]::new)));
        StatefulBeanToCsv<PlayerEpGp> beanToCsv = new StatefulBeanToCsvBuilder<>(writer)
            .withApplyQuotesToAll(false)
            .withMappingStrategy(strategy).build();
        beanToCsv.write(playerEpGpList);
        writer.close();
    }
}
