package ru.tbank.translator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.tbank.translator.model.TranslationRequest;
import ru.tbank.translator.repository.TranslationRepository;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TranslationServiceTest {

    @Mock
    private TranslationRepository translationRepository;

    @Mock
    private YandexTranslateService yandexTranslateService;

    @InjectMocks
    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTranslateWords() {
        TranslationRequest request = new TranslationRequest();
        request.setSourceLang("en");
        request.setTargetLang("ru");
        request.setText("Hello, world!");
        request.setElements(Arrays.asList("Hello", ", ", "world", "!"));
        request.setIpAddress("127.0.0.1");

        when(yandexTranslateService.translateRequest("Hello", "en", "ru"))
                .thenReturn("Привет");
        when(yandexTranslateService.translateRequest("world", "en", "ru"))
                .thenReturn("мир");

        String result = translationService.translateWords(request);

        assertThat(result).isEqualTo("Привет, мир!");

        verify(translationRepository, times(1)).saveTranslation(any(TranslationRequest.class));
    }
}
