package io.hfgbarrigas.delivery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hfgbarrigas.delivery.domain.db.Place;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
public class PlacesIntegrationTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldCreatePlace() throws Exception {
        //NOTE: Due to spring limitations, it is only possible to create places without any routes ahead (unless those routes contain places that already exist).
        //new routes with new places are not supported.
        final Place place = Place
                .builder()
                .name("DUMMY")
                .build();

        //act
        mockMvc.perform(
                post("/places")
                        .with(user("admin")
                                .password("")
                                .authorities(new SimpleGrantedAuthority("ADMIN")))
                        .with(csrf().asHeader())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(place))
        )
                .andExpect(status().isCreated())
                .andReturn();

        //assert
        JsonNode json = objectMapper.readTree(mockMvc.perform(
                get("/places")
                        .with(user("user").password(""))
                        .with(csrf().asHeader())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        List<Place> places = objectMapper.readValue(json.get("_embedded").get("places").toString(), new TypeReference<List<Place>>() {
        });

        assertFalse(places.isEmpty());
        assertTrue(places.stream().anyMatch(p -> "DUMMY".equals(p.getName())));
    }

    @Test
    public void onlyAdminCanCreatePlaces() throws Exception {
        mockMvc.perform(
                post("/places")
                        .with(user("test")
                                .password("")
                                .authorities(new SimpleGrantedAuthority("TEST")))
                        .with(csrf().asHeader())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(io.hfgbarrigas.delivery.domain.db.Place.builder().name("DUMMY").build()))
        )
                .andExpect(status().isForbidden())
                .andReturn();
    }
}
