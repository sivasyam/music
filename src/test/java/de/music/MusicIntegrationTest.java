package de.music;

import de.music.services.MusicService;
import de.music.utils.MusicUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class MusicIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MusicUtils musicUtils;

    @Autowired
    private MusicService musicService;

    @Autowired
    CacheManager cacheManager;

    @Test
    public void testProcessTopTracks_1() {
        musicUtils.processTracks(cacheManager);
    }


    @Test
    public void testTopTracksEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET,"/api/music/top/tracks")
                .param("some-random", "4"))
                .andExpect(status().is(200));
    }

    @Test
    public void testTopTracksEndpoint_404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/music/top/track")
                .param("some-random", "4"))
                .andExpect(status().is(404));
    }

    @Test
    public void testCurrencyAPIForUSD() {
        musicUtils.buildCurrencyCache("USD", cacheManager);
        Assert.assertNotNull(cacheManager.getCache("currency").get("USD"));
    }

    @Test
    public void testCurrencyAPIForGBP() {

        musicUtils.buildCurrencyCache("GBP", cacheManager);
        Assert.assertNotNull(cacheManager.getCache("currency").get("GBP"));
    }

    @Test(expected = Exception.class)
    public void testCurrencyAPIException() {
        musicUtils.buildCurrencyCache("GBP", null);
    }
}
