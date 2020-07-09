package de.music.services;

import de.music.model.Tracks;
import de.music.utils.CSVProcessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import static de.music.model.Constants.GBP_CUR;
import static de.music.model.Constants.USD_CUR;

@Service
@RequiredArgsConstructor
public class MusicService {

    private static final Logger LOGGER = LogManager.getLogger(MusicService.class);

    @Autowired
    CacheManager cacheManager;

    @Autowired
    CSVProcessor csvProcessor;
    @Value("${music.top.tracks.count:5}")
    public int tracksCount;

    @Value("${music.cache.refresh.interval:30000}")
    public String refreshInterval;

    /**
     * Method to process the data from CSV file
     */
    public List<Tracks> processTopTracks() {
        LOGGER.info("Processing data from csv for the dealer {}");
        List<Tracks> tracksList = csvProcessor.processList(cacheManager);

        Collections.sort(tracksList, (o1, o2) -> o2.getAmount().compareTo(o1.getAmount()));

        return tracksList.stream().limit(tracksCount).collect(Collectors.toList());
    }

    /**
     *
     */
    @Scheduled(fixedDelayString = "30000")
    public void refreshCache() {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            LOGGER.warn("Thread interrupted");
        }
        LOGGER.info("refreshing cache");
        csvProcessor.covertToEUR(USD_CUR, cacheManager);
        csvProcessor.covertToEUR(GBP_CUR, cacheManager);

    }
}
