package de.music;

import de.music.services.MusicService;
import de.music.utils.MusicUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static de.music.model.Constants.CURRENCY;

@RunWith(MockitoJUnitRunner.class)
public class MusicTest {

    @InjectMocks
    private MusicUtils musicUtils;

    @InjectMocks
    private MusicService musicService;

    @Mock
    CacheManager cacheManager;

    @Test
    public void testProcessTracksWithCache() {
        setupMockData_2();
        buildCache();
        List result = musicUtils.processTracks(cacheManager);
        Assert.assertNotNull(result);
    }

    @Test
    public void testProcessTracksWithNoCache() {
        setupMockData_2();
        cacheManager = new ConcurrentMapCacheManager(CURRENCY);
        List result = musicUtils.processTracks(cacheManager);
        Assert.assertNotNull(result);
    }

    @Test
    public void testProcessTracks_1_CSV() {
        setupMockData_1();
        buildCache();
        List result = musicUtils.processTracks(cacheManager);
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void testProcessTracks_2_CSV() {
        setupMockData_2();
        buildCache();
        List result = musicUtils.processTracks(cacheManager);
        Assert.assertTrue(result.size() > 0);
    }


    public void setupMockData_1() {
        List<String> tracksList = new ArrayList<>();
        tracksList.add("http://test/");
        tracksList.add("https://storage.googleapis.com/musichub-backend-code-challenge/dsp_streaming_report_us.csv");
        ReflectionTestUtils.setField(musicUtils, "currencyURL", "https://api.ofx.com/PublicSite.ApiService/OFX/spotrate/Individual/$_FROM_CURRENCY_$/EUR/1?format=json");
        ReflectionTestUtils.setField(musicUtils, "tracksCsvUrls", tracksList);
    }

    public void setupMockData_2() {
        List<String> tracksList = new ArrayList<>();
        tracksList.add("https://storage.googleapis.com/musichub-backend-code-challenge/dsp_streaming_report_uk.csv");
        tracksList.add("https://storage.googleapis.com/musichub-backend-code-challenge/dsp_streaming_report_us.csv");
        ReflectionTestUtils.setField(musicUtils, "currencyURL", "https://api.ofx.com/PublicSite.ApiService/OFX/spotrate/Individual/$_FROM_CURRENCY_$/EUR/1?format=json");
        ReflectionTestUtils.setField(musicUtils, "tracksCsvUrls", tracksList);
    }

    public void buildCache() {
        cacheManager = new ConcurrentMapCacheManager(CURRENCY);
        musicUtils.buildCurrencyCache("USD", cacheManager);
        musicUtils.buildCurrencyCache("GBP", cacheManager);
    }

}
