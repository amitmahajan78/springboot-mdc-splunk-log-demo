package com.example.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import jakarta.servlet.ServletException;

@SpringBootTest
@AutoConfigureMockMvc
public class SampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHelloEndpoint() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"))
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void testErrorEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/error"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(content().string("This is a sample error"))
                .andReturn();

        Throwable throwable = result.getResolvedException();
        assertNotNull(throwable, "Expected an exception to be thrown");
        assertTrue(throwable instanceof RuntimeException, "Expected RuntimeException");
        assertEquals("This is a sample error", throwable.getMessage(), "Unexpected error message");
    }
} 