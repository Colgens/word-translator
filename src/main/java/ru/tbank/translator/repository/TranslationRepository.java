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

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS translation_requests (" +
                "id SERIAL PRIMARY KEY, " +
                "ip_address VARCHAR(255), " +
                "text TEXT, " +
                "translated_text TEXT)");

        String sql = "INSERT INTO translation_requests (ip_address, text, translated_text) values (?, ?, ?)";
        jdbcTemplate.update(sql, translationRequest.getIpAddress(), translationRequest.getText(),
                translationRequest.getTranslatedText());
    }
}
