package io.hfgbarrigas.delivery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hfgbarrigas.delivery.domain.api.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
public class PathsIntegrationTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldGetAllPathsByDefault() throws Exception {
        //associate the authority to the user
        List<Path> paths = req("A", "D", null, null, null);

        assertFalse(paths.isEmpty());
        assertTrue(paths.size() == 8);
    }

    @Test
    public void shouldGetShortestPath() throws Exception {
        //associate the authority to the user
        List<Path> paths = req("A", "D", null, null, "SHORTEST");

        assertFalse(paths.isEmpty());
        assertTrue(paths.size() == 1);
        assertTrue(paths.get(0).getPath().size() == 2);
    }

    @Test
    public void shouldGetAllShortestPaths() throws Exception {
        final List<String> shortestPath = Arrays.asList("A", "C", "D");
        final List<String> secondShortestPath = Arrays.asList("A", "B", "D");
        //associate the authority to the user
        List<Path> paths = req("A", "D", null, null, "ALL_SHORTEST");

        assertFalse(paths.isEmpty());
        assertTrue(paths.size() == 2);
        assertTrue(paths.get(0).getPath().size() == 2);
        assertTrue(paths.get(1).getPath().size() == 2);

        assertTrue(paths.get(0).getPath().stream().allMatch(p -> shortestPath.contains(p.getStart().getName())
                && shortestPath.contains(p.getDestination().getName())));

        assertTrue(paths.get(1).getPath().stream().allMatch(p -> secondShortestPath.contains(p.getStart().getName())
                && secondShortestPath.contains(p.getDestination().getName())));
    }

    private List<Path> req(@NotNull String start, @NotNull String end, String cost, String time, String alg) throws Exception {
        StringBuilder base = new StringBuilder(String.format("/paths?start=%s&end=%s", start, end));
        Optional.ofNullable(cost).ifPresent(c -> base.append(String.format("&cost=%s", c)));
        Optional.ofNullable(time).ifPresent(t -> base.append(String.format("&time=%s", t)));
        Optional.ofNullable(alg).ifPresent(a -> base.append(String.format("&algorithm=%s", a)));

        return objectMapper.readValue(mockMvc.perform(
                asyncDispatch(
                        mockMvc.perform(
                                get(base.toString())
                                        .with(user("test").password(""))
                                        .with(csrf().asHeader())
                                        .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), new TypeReference<List<Path>>() {
        });
    }
}
