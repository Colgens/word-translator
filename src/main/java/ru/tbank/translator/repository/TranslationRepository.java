package ru.tbank.translator.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tbank.translator.model.TranslationRequest;

@Repository
public class TranslationRepository {
    private final JdbcTemplate jdbcTemplate;

    public TranslationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveTranslation(TranslationRequest translationRequest) {
        String sql = "INSERT INTO translation_requests (ip_address, original_text, translated_text) values (?, ?, ?)";
        jdbcTemplate.update(sql, translationRequest.getIpAddress(), translationRequest.getText(),
                translationRequest.getTranslatedText());
    }
}
