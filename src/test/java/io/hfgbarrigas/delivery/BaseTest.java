package io.hfgbarrigas.delivery;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

abstract class BaseTest {

    protected static final String CSRF_TOKEN = "X-CSRF-TOKEN";
    protected static final String SESSION = "x-delivery-auth";


    protected static Map<String, String> login(String username,
                                               String password,
                                               MockMvc mockMvc) throws Exception {
        MvcResult result = Objects.requireNonNull(mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                        new BasicNameValuePair("username", username),
                        new BasicNameValuePair("password", password)
                )))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("X-CSRF-TOKEN"))
                .andExpect(cookie().exists("x-delivery-auth"))
                .andReturn());

        assertFalse("Session must exist", Strings.isNullOrEmpty(result.getResponse().getCookie(SESSION).getValue()));
        assertFalse("Csrf token must exist", Strings.isNullOrEmpty(result.getResponse().getHeader(CSRF_TOKEN)));

        return ImmutableMap.of(
                SESSION, result.getResponse().getCookie(SESSION).getValue(),
                CSRF_TOKEN, result.getResponse().getHeader(CSRF_TOKEN)
        );
    }
}
