package ru.tbank.translator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import static org.junit.jupiter.api.Assertions.*;

class YandexTranslateServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private YandexTranslateService yandexTranslateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTranslateRequestSuccess() {
        String text = "Hello";
        String sourceLang = "en";
        String targetLang = "ru";

        YandexTranslateService.YandexTranslateResponse mockResponse = new YandexTranslateService.YandexTranslateResponse();
        YandexTranslateService.Translation translation = new YandexTranslateService.Translation();
        translation.setText("Привет");
        mockResponse.setTranslations(java.util.Collections.singletonList(translation));

        when(restTemplate.postForObject(
                        anyString(),
                        any(HttpEntity.class),
                        eq(YandexTranslateService.YandexTranslateResponse.class)
                )
        ).thenReturn(mockResponse);

        String result = yandexTranslateService.translateRequest(text, sourceLang, targetLang);

        assertEquals("Привет", result);
    }

    @Test
    void testTranslateRequestEmptyResponse() {
        String text = "Hello";
        String sourceLang = "en";
        String targetLang = "ru";


        YandexTranslateService.YandexTranslateResponse mockResponse =
                new YandexTranslateService.YandexTranslateResponse();
        mockResponse.setTranslations(java.util.Collections.emptyList());

        when(restTemplate.postForObject(
                        anyString(),
                        any(HttpEntity.class),
                        eq(YandexTranslateService.YandexTranslateResponse.class)
                )
        ).thenReturn(mockResponse);


        String result = yandexTranslateService.translateRequest(text, sourceLang, targetLang);


        assertEquals("", result);
    }

    @Test
    void testTranslateRequestUnauthorized() {
        String text = "Hello";
        String sourceLang = "en";
        String targetLang = "ru";


        when(restTemplate.postForObject(
                        anyString(),
                        any(HttpEntity.class),
                        eq(YandexTranslateService.YandexTranslateResponse.class)
                )
        ).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));


        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                yandexTranslateService.translateRequest(text, sourceLang, targetLang));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("401 Unauthorized", exception.getMessage());
    }
}
