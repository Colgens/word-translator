package ru.tbank.translator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.tbank.translator.model.TranslationRequest;
import ru.tbank.translator.repository.TranslationRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
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
        translationService = new TranslationService(translationRepository, yandexTranslateService);
    }

    @Test
    void testTranslateWords() {
        TranslationRequest request = new TranslationRequest();
        request.setSourceLang("en");
        request.setTargetLang("ru");
        request.setText("Hello, world!");
        request.setElements(List.of("Hello", ", ", "world", "!"));
        request.setIpAddress("127.0.0.1");

        when(yandexTranslateService.translateRequest("Hello", "en", "ru")).thenReturn("Привет");
        when(yandexTranslateService.translateRequest("world", "en", "ru")).thenReturn("мир");

        String translatedText = translationService.translateWords(request);

        assertEquals("Привет, мир!", translatedText);

        ArgumentCaptor<TranslationRequest> requestCaptor = ArgumentCaptor.forClass(TranslationRequest.class);
        verify(translationRepository, times(1)).saveTranslation(requestCaptor.capture());
        TranslationRequest savedRequest = requestCaptor.getValue();

        assertEquals("127.0.0.1", savedRequest.getIpAddress());
        assertEquals("Hello, world!", savedRequest.getText());
        assertEquals("Привет, мир!", savedRequest.getTranslatedText());
    }

    @Test
    void testTranslateWordsWithMultipleThreads() throws NoSuchFieldException, IllegalAccessException {

        TranslationRequest request = new TranslationRequest();
        request.setSourceLang("en");
        request.setTargetLang("ru");
        request.setText("Hello, world!");
        request.setElements(List.of("Hello", ", ", "world", "!"));
        request.setIpAddress("127.0.0.1");

        ExecutorService mockExecutorService = spy(Executors.newFixedThreadPool(10));

        Field executorServiceField = TranslationService.class.getDeclaredField("executorService");
        executorServiceField.setAccessible(true);
        executorServiceField.set(translationService, mockExecutorService);

        when(yandexTranslateService.translateRequest("Hello", "en", "ru")).thenReturn("Привет");
        when(yandexTranslateService.translateRequest("world", "en", "ru")).thenReturn("мир");

        String translatedText = translationService.translateWords(request);

        assertEquals("Привет, мир!", translatedText);

        verify(mockExecutorService, atLeast(2)).execute(any(Runnable.class));
    }
}
