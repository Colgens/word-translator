package ru.tbank.translator.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.tbank.translator.model.TranslationRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class TranslationRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TranslationRepository translationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveTranslation() {
        TranslationRequest translationRequest = new TranslationRequest();
        translationRequest.setIpAddress("127.0.0.1");
        translationRequest.setText("Hello, world!");
        translationRequest.setTranslatedText("Привет, мир!");

        translationRepository.saveTranslation(translationRequest);

        verify(jdbcTemplate, times(1)).update(any(String.class), any(Object[].class));
    }
}