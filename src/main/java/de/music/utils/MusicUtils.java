package de.music.utils;

import com.google.gson.JsonParser;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import de.music.model.Tracks;

import org.apache.commons.collections.CollectionUtils;
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

import static de.music.model.Constants.CSV_DELIMITER;
import static de.music.model.Constants.CURRENCY;
import static de.music.model.Constants.CUSTOMER_AMOUNT_FIELD;
import static de.music.model.Constants.FROM_CURRENCY;
import static de.music.model.Constants.GBP_CUR;
import static de.music.model.Constants.USD_CUR;

/**
 * Class to process tracks and fetch currency from api
 */
@Component
public class MusicUtils {
    private static final Logger LOGGER = LogManager.getLogger(MusicUtils.class);

    @Value("${music.currency.url}")
    public String currencyURL;

    @Value("#{'${music.tracks.csv}'.split(',')}")
    public List<String> tracksCsvUrls;

    /**
     * Process music tracks method, read track csv files from storage and process further
     *
     * @param cacheManager
     * @return
     */
    public List<Tracks> processTracks(CacheManager cacheManager) {
        List<CSVReader> csvReaders = tracksCsvUrls.stream().map(p -> {
            CSVParser parser = new CSVParserBuilder().withSeparator(CSV_DELIMITER).build();
            Reader reader = buildReader(p);
            return reader != null ? new CSVReaderBuilder(buildReader(p)).withCSVParser(parser).build() : null;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(csvReaders))
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Problem while reading CSV files");

        return processReader(csvReaders, cacheManager);
    }

    /**
     * Method to build the csv file reader
     */
    private Reader buildReader(String urlName) {
        Reader reader = null;
        try {
            URL url = new URL(urlName);
            reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
        } catch (IOException e) {
            LOGGER.error("Unable to process the CSV file {}", urlName, e);
        }
        return reader;
    }

    /**
     *
     */
    private List<Tracks> processReader(List<CSVReader> csvReader, CacheManager cacheManager) {
        try {
            List<Tracks> tracksList = new ArrayList<>();
            csvReader.stream().forEach(reader -> {
                processCSVReader(reader, tracksList, cacheManager);
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
    private void processCSVReader(CSVReader reader, List<Tracks> tracksList, CacheManager cacheManager) {
        try {
            if (reader == null) {
                LOGGER.error("Null response received from csv reader ");
            } else {
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
            }
        } catch (IOException | CsvException e) {
            LOGGER.error("Unable to process the csv request ", e);
        }
    }

    /**
     * Method to update the amount if the track is already exists
     */
    private void buildExistingTrack(String[] trackArray, Tracks tracks, CacheManager cacheManager) {
        Float updatedPrice = tracks.getAmount() + convertCurrencyToEur(trackArray[5], trackArray[4], cacheManager);
        tracks.setAmount(updatedPrice);
    }

    /**
     * Method to build track
     */
    private Tracks buildTrack(String[] trackArray, CacheManager cacheManager) {
        return Tracks.builder().ISRC(trackArray[0])
                .trackName(trackArray[1])
                .artistName(trackArray[2])
                .amount(convertCurrencyToEur(trackArray[5], trackArray[4], cacheManager))
                .build();
    }

    /**
     * Method to process converting the amount from USD/GBP to EUR
     */
    private Float convertCurrencyToEur(String currency, String amount, CacheManager cacheManager) {
        return USD_CUR.equals(currency)
                ? Float.parseFloat(amount) * getCurrencyFromCache(cacheManager, USD_CUR)
                : Float.parseFloat(amount) * getCurrencyFromCache(cacheManager, GBP_CUR);
    }

    /**
     * Method to call currency converter API
     */
    public ResponseEntity<String> currencyAPICall(String currencyType) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(currencyURL.replace(FROM_CURRENCY, currencyType), String.class);
    }

    /**
     * Method to build cache with USD and GBP from currency API
     */
    public void buildCurrencyCache(String currencyType, CacheManager cacheManager) {
        try {
            ResponseEntity<String> currency = currencyAPICall(currencyType);
            if (currency.getStatusCode().equals(HttpStatus.OK)) {
                buildCache(currency.getBody(), currencyType, cacheManager);
            } else {
                LOGGER.error("Unable to fetch price from API ");
            }
        } catch (Exception e) {
            LOGGER.error("Unable to process currency ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to process csv, exception" + e);
        }
    }

    /**
     * Method to put the api values to cache
     */
    private void buildCache(String currency, String currencyType, CacheManager cacheManager) {
        LOGGER.info("Fetching Currency from api for Currency type {}", currencyType);
        cacheManager.getCache(CURRENCY).put(currencyType,
                new JsonParser().parse(currency).getAsJsonObject().get(CUSTOMER_AMOUNT_FIELD).getAsFloat());

    }

    /**
     * Method to read price from cache
     */
    public Float getCurrencyFromCache(CacheManager cacheManager, String currencyType) {
        try {
            Float price = (Float) cacheManager.getCache(CURRENCY).get(currencyType).get();
            if (price == null) {
                price = getLiveRate(currencyType);
            }
            return price;
        } catch (Exception e) {
            LOGGER.warn("Failed to fetch data from cache, trying from direct url {}", currencyType);
            return getLiveRate(currencyType);
        }
    }

    /**
     * Method to fetch live currency rate if the cache fails
     */
    private Float getLiveRate(String currencyType) {
        String currencyResponse = currencyAPICall(currencyType).getBody();
        return new JsonParser().parse(currencyResponse).getAsJsonObject().get(CUSTOMER_AMOUNT_FIELD).getAsFloat();

    }
}
