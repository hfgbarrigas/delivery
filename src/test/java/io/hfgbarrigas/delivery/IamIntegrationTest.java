package io.hfgbarrigas.delivery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.hfgbarrigas.delivery.domain.api.Authority;
import io.hfgbarrigas.delivery.domain.api.User;
import io.hfgbarrigas.delivery.services.Users;
import io.hfgbarrigas.delivery.utils.ValidationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.Cookie;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
public class IamIntegrationTest extends BaseTest {

    @Autowired
    private Users users;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    public void shouldBlockUnauthorizedRequestsToUsers() throws Exception {
        mockMvc.perform(get("/users")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldCreateUserWithNoAuthorities() throws Exception {
        //act
        User user1 = createUser(User.builder()
                .username("testing")
                .password("testing")
                .build());

        //assert
        assertTrue(ValidationUtils.isNullOrEmpty(user1.getAuthorities()));
        assertFalse(Strings.isNullOrEmpty(user1.getPassword()));
        assertTrue(user1.getId() != null);
        assertTrue(user1.getUsername() != null);
    }

    @Test
    public void loggedInUsersShouldSeeDetails() throws Exception {
        //act
        User user1 = createUser(User.builder()
                .username("details")
                .password("details")
                .build());

        Map<String, String> session = login(user1.getUsername(), "details", mockMvc);

        User user2 = objectMapper.readValue(
                mockMvc.perform(
                        get("/users/{id}", user1.getId())
                                .accept(MediaType.APPLICATION_JSON)
                                .header(CSRF_TOKEN, session.get(CSRF_TOKEN))
                                .cookie(new Cookie(SESSION, session.get(SESSION))))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(), User.class);

        //assert that all mandatory attributes are present, spring data rest does not expose the id field in the _self
        assertFalse(Strings.isNullOrEmpty(user2.getPassword()));
        assertFalse(Strings.isNullOrEmpty(user2.getUsername()));
        assertTrue(user2.getUsername().equals(user1.getUsername()));
    }

    @Test
    public void shouldReceiveConflictOnExistingUser() throws Exception {
        //prepare
        User user = User.builder()
                .username("testingConflict")
                .password("testingConflict")
                .build();

        mockMvc.perform(
                asyncDispatch(
                        postRequest(user, "/users")
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn()
                ))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        //act & assert
        postRequest(user, "/users")
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReceivedBadRequestOnMissingUsername() throws Exception {
        User user = User.builder()
                .password("testingBadRequest")
                .build();

        postRequest(user, "/users")
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReceivedBadRequestOnMissingPassword() throws Exception {
        User user = User.builder()
                .username("testingBadRequest")
                .build();

        postRequest(user, "/users")
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldUpdateUserDetails() throws Exception {

        User updatedVersion = User.builder()
                .username("updateVersion")
                .password("updatedVersion")
                .firstName("updatedVersion")
                .build();

        //create the user
        User user1 = createUser(User.builder()
                .username("update")
                .password("update")
                .build());

        //login
        Map<String, String> session = login(user1.getUsername(), "update", mockMvc);

        //update the account

        //put request
        MvcResult mvcResult = mockMvc.perform(put("/users/{id}", user1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header(CSRF_TOKEN, session.get(CSRF_TOKEN))
                .cookie(new Cookie(SESSION, session.get(SESSION)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updatedVersion)))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        //wait for the result of the async call
        User user2 = objectMapper.readValue(
                mockMvc.perform(
                        asyncDispatch(mvcResult))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(), User.class);

        //assert that all mandatory attributes are present, spring data rest does not expose the id field in the _self
        assertFalse(Strings.isNullOrEmpty(user2.getPassword()));
        assertFalse(Strings.isNullOrEmpty(user2.getUsername()));
        assertTrue(user2.getUsername().equals(updatedVersion.getUsername()));
        assertTrue(bCryptPasswordEncoder.matches(updatedVersion.getPassword(), user2.getPassword()));
    }

    @Test
    public void shouldReceiveErrorWhenUpdatingNonExistingUser() throws Exception {

        User updatedVersion = User.builder()
                .username("updateVersion")
                .password("updatedVersion")
                .firstName("updatedVersion")
                .build();

        //create the user
        User user1 = createUser(User.builder()
                .username("updateError")
                .password("updateError")
                .build());

        //login
        Map<String, String> session = login(user1.getUsername(), "updateError", mockMvc);

        //update the account

        //put request
        mockMvc.perform(put("/users/1000012")
                .accept(MediaType.APPLICATION_JSON)
                .header(CSRF_TOKEN, session.get(CSRF_TOKEN))
                .cookie(new Cookie(SESSION, session.get(SESSION)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updatedVersion)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReceiveErrorUpdatingUserDifferentFromSession() throws Exception {

        User updatedVersion = User.builder()
                .username("updateVersion")
                .password("updatedVersion")
                .firstName("updatedVersion")
                .build();

        //create the user
        User user1 = createUser(User.builder()
                .username("updateError1")
                .password("updateError1")
                .build());

        User user2 = createUser(User.builder()
                .username("updateError2")
                .password("updateError2")
                .build());

        //login
        Map<String, String> session = login(user1.getUsername(), "updateError1", mockMvc);

        //update the account user2

        //put request
        mockMvc.perform(put("/users/{id}", user2.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header(CSRF_TOKEN, session.get(CSRF_TOKEN))
                .cookie(new Cookie(SESSION, session.get(SESSION)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updatedVersion)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        mockMvc.perform(
                asyncDispatch(
                        mockMvc.perform(delete("/users/{id}", 1)
                                .with(user("admin")
                                        .password("")
                                        .authorities(new SimpleGrantedAuthority("ADMIN")))
                                .with(csrf().asHeader())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldDeleteErrorForNonAdmins() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1)
                .with(user("test")
                        .password("")
                        .authorities(new SimpleGrantedAuthority("TEST")))
                .with(csrf().asHeader())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void shouldAddAuthorities() throws Exception {
        //create the authority
        final String authority = "POTATO";
        createAuthority(authority);

        //validate that exists
        JsonNode json = objectMapper.readTree(mockMvc.perform(get("/authorities")
                .with(user("admin")
                        .password("")
                        .authorities(new SimpleGrantedAuthority("ADMIN")))
                .with(csrf().asHeader())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        List<Authority> authorities = objectMapper.readValue(json.get("_embedded").get("authorities").toString(), new TypeReference<List<Authority>>() {
        });

        assertFalse(authorities.isEmpty());
        assertTrue(authorities.stream().anyMatch(a -> authority.equals(a.getName())));
    }

    @Test
    public void shouldAddUserAuthorities() throws Exception {
        final String authority = "DUMMY";
        //create the user
        User user1 = createUser(User.builder()
                .username("userAuthorities")
                .password("userAuthorities")
                .build());

        //create the authority
        createAuthority(authority);

        //associate the authority to the user
        mockMvc.perform(
                asyncDispatch(
                        mockMvc.perform(
                                post("/users/{id}/authorities", user1.getId())
                                        .with(user("admin")
                                                .password("")
                                                .authorities(new SimpleGrantedAuthority("ADMIN")))
                                        .with(csrf().asHeader())
                                        .accept(MediaType.APPLICATION_JSON)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsBytes(Collections.singletonList(authority)))
                        )
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn()))
                .andExpect(status().isNoContent());

        //verify user has the appointed authority

        JsonNode json = objectMapper.readTree(mockMvc.perform(
                get("/users/{id}/authorities", user1.getId())
                        .with(user("admin")
                                .password("")
                                .authorities(new SimpleGrantedAuthority("ADMIN")))
                        .with(csrf().asHeader())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        List<Authority> authorities = objectMapper.readValue(json.get("_embedded").get("authorities").toString(), new TypeReference<List<Authority>>() {
        });

        assertFalse(authorities.isEmpty());
        assertTrue(authorities.stream().anyMatch(a -> authority.equals(a.getName())));
    }

    private <T> ResultActions postRequest(T payload, String path) throws Exception {
        return mockMvc.perform(post(path)
                .content(objectMapper.writeValueAsBytes(payload))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    private User createUser(User user) throws Exception {
        //create the user
        return objectMapper.readValue(mockMvc.perform(
                asyncDispatch(
                        postRequest(user, "/users")
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn()
                ))
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(header().exists("Location"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
                , User.class);
    }

    private void createAuthority(String authority) throws Exception {
        //create the authority
        mockMvc.perform(
                asyncDispatch(
                        mockMvc.perform(post("/authorities")
                                .with(user("admin")
                                        .password("")
                                        .authorities(new SimpleGrantedAuthority("ADMIN")))
                                .with(csrf().asHeader())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(Collections.singletonList(authority))))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/authorities"));
    }
}
