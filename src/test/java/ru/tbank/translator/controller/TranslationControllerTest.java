package ru.tbank.translator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.tbank.translator.model.TranslationRequest;
import ru.tbank.translator.service.TranslationService;
import ru.tbank.translator.service.YandexLanguageService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TranslationController.class)
class TranslationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslationService translationService;

    @MockBean
    private YandexLanguageService yandexLanguageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTranslateWithValidRequest() throws Exception {
        TranslationRequest request = new TranslationRequest();
        request.setSourceLang("ru");
        request.setTargetLang("en");
        request.setText("Привет");
        request.setElements(Collections.singletonList("Привет"));
        request.setIpAddress("127.0.0.1");

        Set<String> availableLanguages = new HashSet<>();
        availableLanguages.add("ru");
        availableLanguages.add("en");

        when(yandexLanguageService.getAvailableLanguages()).thenReturn(availableLanguages);
        when(translationService.translateWords(any(TranslationRequest.class))).thenReturn("Hello");

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello"));
    }

    @Test
    void testTranslateWithInvalidSourceLanguage() throws Exception {
        TranslationRequest request = new TranslationRequest();
        request.setSourceLang("invalid");
        request.setTargetLang("en");
        request.setText("Привет");

        Set<String> availableLanguages = new HashSet<>();
        availableLanguages.add("ru");
        availableLanguages.add("en");

        when(yandexLanguageService.getAvailableLanguages()).thenReturn(availableLanguages);

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Не найден язык исходного сообщения"));
    }

    @Test
    void testTranslateWithInvalidTargetLanguage() throws Exception {
        TranslationRequest request = new TranslationRequest();
        request.setSourceLang("ru");
        request.setTargetLang("invalid");
        request.setText("Привет");

        Set<String> availableLanguages = new HashSet<>();
        availableLanguages.add("ru");
        availableLanguages.add("en");

        when(yandexLanguageService.getAvailableLanguages()).thenReturn(availableLanguages);

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Не найден целевой язык для перевода"));
    }

    @Test
    void testTranslateWithEmptyText() throws Exception {
        TranslationRequest request = new TranslationRequest();
        request.setSourceLang("ru");
        request.setTargetLang("en");
        request.setText("");

        Set<String> availableLanguages = new HashSet<>();
        availableLanguages.add("ru");
        availableLanguages.add("en");

        when(yandexLanguageService.getAvailableLanguages()).thenReturn(availableLanguages);

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Текст не может быть пустым"));
    }

    @Test
    void testTranslateWithMissingSourceLang() throws Exception {
        TranslationRequest request = new TranslationRequest();
        request.setTargetLang("en");
        request.setText("Привет");

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Исходный язык не указан"));
    }

    @Test
    void testTranslateWithMissingTargetLang() throws Exception {
        TranslationRequest request = new TranslationRequest();
        request.setSourceLang("ru");
        request.setText("Привет");


        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Целевой язык не указан"));
    }

    @Test
    void testTranslateWithInvalidJson() throws Exception {
        String invalidJson = "{invalid}";

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Некорректный формат JSON"));
    }

    private final TranslationController translationController = new TranslationController(null, null);
    @Test
    void testSplitTextWithEnglishSentence() {
        String text = "Hello, world!";
        List<String> expected = Arrays.asList("Hello", ", ", "world", "!");
        List<String> result = translationController.splitTextIntoWordsAndPunctuationMarks(text);
        assertEquals(expected, result);
    }

    @Test
    void testSplitTextWithRussianSentence() {
        String text = "Привет, мир!";
        List<String> expected = Arrays.asList("Привет", ", ", "мир", "!");
        List<String> result = translationController.splitTextIntoWordsAndPunctuationMarks(text);
        assertEquals(expected, result);
    }

    @Test
    void testSplitTextWithMixedSentence() {
        String text = "Hello, мир!";
        List<String> expected = Arrays.asList("Hello", ", ", "мир", "!");
        List<String> result = translationController.splitTextIntoWordsAndPunctuationMarks(text);
        assertEquals(expected, result);

    }

    @Test
    void testSplitTextWithPunctuationOnly() {
        String text = ",.!?";
        List<String> expected = List.of(",.!?");
        List<String> result = translationController.splitTextIntoWordsAndPunctuationMarks(text);
        assertEquals(expected, result);
    }

    @Test
    void testSplitTextWithSpacesOnly() {
        String text = "   ";
        List<String> expected = List.of("   ");
        List<String> result = translationController.splitTextIntoWordsAndPunctuationMarks(text);
        assertEquals(expected, result);
    }

    @Test
    void testSplitTextWithEmptyString() {
        String text = "";
        List<String> expected = List.of();
        List<String> result = translationController.splitTextIntoWordsAndPunctuationMarks(text);
        assertEquals(expected, result);
    }

    @Test
    void testSplitTextWithComplexSentence() {
        String text = "Hello, Кен! How are you?";
        List<String> expected = Arrays.asList("Hello", ", ", "Кен", "! ", "How", " ", "are", " ", "you", "?");
        List<String> result = translationController.splitTextIntoWordsAndPunctuationMarks(text);
        assertEquals(expected, result);
    }


}

