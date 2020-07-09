package de.music.controllers;

import de.music.services.MusicService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/music")
public class MusicController {
    private static final Logger LOGGER = LogManager.getLogger(MusicController.class);

    @Autowired
    private MusicService musicService;

    /**
     * Method to upload publishing CSV file
     */
    @PostMapping(value = "/top/tracks")
    public ResponseEntity<?> topTracks() {
        LOGGER.info("Processing Top 5 tracks {}");

        return ResponseEntity.ok().body(musicService.processTopTracks());
    }
}
