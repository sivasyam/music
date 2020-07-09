package de.music.controllers;

import de.music.model.Tracks;
import de.music.services.MusicService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * Music tracks controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/music")
@Api(value = "Music tracks controller")
public class MusicController {
    private static final Logger LOGGER = LogManager.getLogger(MusicController.class);

    @Autowired
    private MusicService musicService;

    /**
     * Method to process top tracks request
     */
    @GetMapping(value = "/top/tracks")
    @ApiOperation(value = "Get top 5 tracks from the given csv files")
    @ApiResponses(value = {@ApiResponse(code = 200, response = List.class, message = "Music tracks response received"),
            @ApiResponse(code = 404, message = "No tracks found"), @ApiResponse(code = 500, message = "Internal error")})
    public ResponseEntity<?> topTracks() {
        LOGGER.info("Processing Top 5 tracks {}");

        return ResponseEntity.ok().body(musicService.processTopTracks());
    }
}
