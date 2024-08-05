package ru.tbank.translator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class YandexLanguageServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private YandexLanguageService yandexLanguageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAvailableLanguagesSuccess() {
        // Given
        YandexLanguageService.YandexLanguagesResponse mockResponse = new YandexLanguageService.YandexLanguagesResponse();
        YandexLanguageService.Language lang1 = new YandexLanguageService.Language();
        lang1.setCode("en");
        lang1.setName("English");
        YandexLanguageService.Language lang2 = new YandexLanguageService.Language();
        lang2.setCode("fr");
        lang2.setName("French");

        mockResponse.setLanguages(List.of(lang1, lang2));

        when(restTemplate.postForObject(
                anyString(),
                any(HttpEntity.class),
                any(Class.class)
        )).thenReturn(mockResponse);

        // When
        Set<String> languages = yandexLanguageService.getAvailableLanguages();

        // Then
        Set<String> expectedLanguages = new HashSet<>();
        expectedLanguages.add("en");
        expectedLanguages.add("fr");

        assertEquals(expectedLanguages, languages);
    }

    @Test
    void testGetAvailableLanguagesCache() {
        // Given
        YandexLanguageService.YandexLanguagesResponse mockResponse = new YandexLanguageService.YandexLanguagesResponse();
        YandexLanguageService.Language lang1 = new YandexLanguageService.Language();
        lang1.setCode("es");
        lang1.setName("Spanish");

        mockResponse.setLanguages(List.of(lang1));

        when(restTemplate.postForObject(
                anyString(),
                any(HttpEntity.class),
                any(Class.class)
        )).thenReturn(mockResponse);

        yandexLanguageService.getAvailableLanguages(); // Cache languages

        // Modify mock response
        YandexLanguageService.YandexLanguagesResponse newMockResponse = new YandexLanguageService.YandexLanguagesResponse();
        YandexLanguageService.Language lang2 = new YandexLanguageService.Language();
        lang2.setCode("de");
        lang2.setName("German");

        newMockResponse.setLanguages(List.of(lang2));
        when(restTemplate.postForObject(
                anyString(),
                any(HttpEntity.class),
                any(Class.class)
        )).thenReturn(newMockResponse);

        // When
        Set<String> languages = yandexLanguageService.getAvailableLanguages();

        // Then
        Set<String> expectedLanguages = new HashSet<>();
        expectedLanguages.add("es");

        assertEquals(expectedLanguages, languages);
    }
}
