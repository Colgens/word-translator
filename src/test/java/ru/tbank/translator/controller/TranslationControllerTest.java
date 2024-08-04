package ru.tbank.translator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.tbank.translator.model.TranslationRequest;
import ru.tbank.translator.service.TranslationService;
import ru.tbank.translator.service.YandexLanguageService;


import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TranslationController.class)
class TranslationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslationService translationService;

    @MockBean
    private YandexLanguageService yandexLanguageService;

    @BeforeEach
    void setUp() {
        when(yandexLanguageService.getAvailableLanguages()).thenReturn(Set.of("en", "ru", "fr"));
    }

    @Test
    void testTranslateValidRequest() throws Exception {
        String requestBody = "en → ru\nHello, world!";
        when(translationService.translateWords(any(TranslationRequest.class))).thenReturn("Привет, мир!");

        mockMvc.perform(MockMvcRequestBuilders.post("/translate")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Привет, мир!"));
    }

    @Test
    void testTranslateInvalidRequestFormat() throws Exception {
        String requestBody = "Invalid format";

        mockMvc.perform(MockMvcRequestBuilders.post("/translate")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Некорректный формат запроса"));
    }

    @Test
    void testTranslateMissingArrow() throws Exception {
        String requestBody = "en ru\nHello, world!";

        mockMvc.perform(MockMvcRequestBuilders.post("/translate")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string("Исходный и целевой языки должны быть разделены символом '→'"));
    }

    @Test
    void testTranslateUnknownSourceLanguage() throws Exception {
        String requestBody = "xx → ru\nHello, world!";

        mockMvc.perform(MockMvcRequestBuilders.post("/translate")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Не найден язык исходного сообщения"));
    }

    @Test
    void testTranslateUnknownTargetLanguage() throws Exception {
        String requestBody = "en → xx\nHello, world!";

        mockMvc.perform(MockMvcRequestBuilders.post("/translate")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Не найден целевой язык для перевода"));
    }
}