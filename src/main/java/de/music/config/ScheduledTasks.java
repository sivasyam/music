package de.music.config;


import de.music.utils.MusicUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static de.music.model.Constants.GBP_CUR;
import static de.music.model.Constants.USD_CUR;

/**
 *
 */
@Configuration
@Component
public class ScheduledTasks implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    MusicUtils musicUtils;

    @Autowired
    CacheManager cacheManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        logger.info("Fetching cache on startup");
        musicUtils.buildCurrencyCache(USD_CUR, cacheManager);
        musicUtils.buildCurrencyCache(GBP_CUR, cacheManager);
    }
}
