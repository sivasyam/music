package de.music.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import de.music.model.Tracks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.music.model.Constants.CURRENCY;
import static de.music.model.Constants.CUSTOMER_AMOUNT_FIELD;
import static de.music.model.Constants.CV_DELIMITER;
import static de.music.model.Constants.FROM_CURRENCY;
import static de.music.model.Constants.GBP_CUR;
import static de.music.model.Constants.USD_CUR;

/**
 * Class to process the CSV
 */
@Component
public class CSVProcessor {
    private static final Logger LOGGER = LogManager.getLogger(CSVProcessor.class);

    @Value("${music.currency.url}")
    public String currencyURL;

    @Value("#{'${music.tracks.csv}'.split(',')}")
    public List<String> csvURLs;

    public List<Tracks> processList(CacheManager cacheManager) {

        List<CSVReader> readersList = csvURLs.stream().map(p -> {
            URL url = null;
            Reader reader = null;
            try {
                url = new URL(p);
                reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
            } catch (IOException e) {
                LOGGER.error("Unable to process the CSV file ", e);
            }
            CSVParser parser = new CSVParserBuilder().withSeparator(CV_DELIMITER).build();

            return new CSVReaderBuilder(reader).withCSVParser(parser).build();
        }).collect(Collectors.toList());
        return processCSV(readersList, cacheManager);
    }

    private List<Tracks> processCSV(List<CSVReader> csvReader, CacheManager cacheManager) {
        try {
            List<Tracks> tracksList = new ArrayList<>();
            csvReader.stream().forEach(reader -> {
                try {
                    reader.skip(1);
                    reader.readAll().stream().forEach(p -> {
                        long count = tracksList.stream().filter(aa -> aa.getISRC().equals(p[0])).map(ap -> {
                            buildExistingTrack(p, ap, cacheManager);
                            return ap;
                        }).count();
                        if (count <= 0) {
                            tracksList.add(buildTrack(p, cacheManager));
                            return;
                        }
                    });
                } catch (IOException e) {
                    LOGGER.error("Unable to process the  csv request ", e);
                } catch (CsvException e) {
                    LOGGER.error("Unable to process the  csv request ", e);
                }
            });
            return tracksList;
        } catch (Exception e) {
            LOGGER.error("Unable to process the  csv request ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to process csv, exception" + e);
        }
    }

    /**
     *
     */
    private void buildExistingTrack(String[] trackArray, Tracks tracks, CacheManager cacheManager) {
        Float price = USD_CUR.equals(trackArray[5])
                ? Float.parseFloat(trackArray[4]) * getUSDCurrencyCache(cacheManager)
                : Float.parseFloat(trackArray[4]) * getGBPCurrencyCache(cacheManager);
        Float updatedPrice = tracks.getAmount() + price;
        tracks.setAmount(updatedPrice);
    }

    /**
     *
     */
    private Tracks buildTrack(String[] trackArray, CacheManager cacheManager) {
        return Tracks.builder().ISRC(trackArray[0])
                .trackName(trackArray[1])
                .artistName(trackArray[2])
                .units(Long.parseLong(trackArray[3]))
                .amount(USD_CUR.equals(trackArray[5])
                        ? Float.parseFloat(trackArray[4]) * getUSDCurrencyCache(cacheManager)
                        : Float.parseFloat(trackArray[4]) * getGBPCurrencyCache(cacheManager))
                .build();
    }

    /**
     *
     */
    public void covertToEUR(String currencyType, CacheManager cacheManager) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> currency = restTemplate.getForEntity(currencyURL.replace(FROM_CURRENCY, currencyType), String.class);
            JsonObject jsonObject = new JsonParser().parse(currency.getBody()).getAsJsonObject();
            if (currency.getStatusCode().equals(HttpStatus.OK)) {
                LOGGER.info("Fetching Currency from api for Currency type {}, value {}", currencyType, jsonObject.get(CUSTOMER_AMOUNT_FIELD).getAsFloat());
                cacheManager.getCache(CURRENCY).put(currencyType, jsonObject.get(CUSTOMER_AMOUNT_FIELD).getAsFloat());
            }
        } catch (Exception e) {
            LOGGER.error("Unable to process currency ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to process csv, exception" + e);
        }
    }

    /**
     *
     */
    public Float getUSDCurrencyCache(CacheManager cacheManager) {
        Float usd = (Float) cacheManager.getCache(CURRENCY).get(USD_CUR).get();
        if (usd == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error occurred while fetching data from cache ");
        return usd;
    }

    public Float getGBPCurrencyCache(CacheManager cacheManager) {
        Float gbp = (Float) cacheManager.getCache(CURRENCY).get(GBP_CUR).get();
        if (gbp == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error occurred while fetching data from cache ");
        return gbp;
    }

}
