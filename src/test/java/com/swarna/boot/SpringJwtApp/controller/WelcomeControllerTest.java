package com.swarna.boot.SpringJwtApp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swarna.boot.SpringJwtApp.model.AuthRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class WelcomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGenerateToken() throws Exception {

        String payload = new ObjectMapper().writeValueAsString(new AuthRequest("javatechie", "password"));

        mockMvc.perform(post("/authenticate")
                        .accept(TEXT_PLAIN_VALUE)
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    void testGenerateTokenIOException() throws Exception {

        String payload = new ObjectMapper().writeValueAsString(new AuthRequest("javatec", "passrd"));

        mockMvc.perform(post("/authenticate")
                        .accept(TEXT_PLAIN_VALUE)
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}