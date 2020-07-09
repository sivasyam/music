package de.music;

import de.music.utils.CSVProcessor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
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
    private CSVProcessor csvProcessor;

    @Autowired
    CacheManager cacheManager;

    @Test
    public void testTopTracksEndpoint() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/music/top/tracks")
                .param("some-random", "4"))
                .andExpect(status().is(200));
    }

    @Test
    public void testCurrencyAPIforUSD() {
        csvProcessor.covertToEUR("USD", cacheManager);
        Assert.assertNotNull(cacheManager.getCache("currency").get("USD"));
    }

    @Test
    public void testCurrencyAPIforGBP() {

        csvProcessor.covertToEUR("GBP", cacheManager);
        Assert.assertNotNull(cacheManager.getCache("currency").get("GBP"));
    }

    @Test(expected = Exception.class)
    public void testCurrencyAPIException() {

        csvProcessor.covertToEUR("GBP", null);

    }
   /*  @Test
    public void testSaveVehicle() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        List<VehicleDTO> vehicleDTOList = new ArrayList<>();
        vehicleDTOList.add(VehicleDTO.builder().model("c class").make("benz").price(12234.00).kw(123L).year(2020).color("White").code("1a").build());
        String requestJson = ow.writeValueAsString(vehicleDTOList);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/vehicle/vehicle_listings/1")
                .content(requestJson).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void testSaveVehicleFailureCase() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        List<VehicleDTO> vehicleDTOList = new ArrayList<>();
        String requestJson = ow.writeValueAsString(vehicleDTOList);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/vehicle/vehicle_listings/1")
                .content(requestJson).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError());
    }*/
}
