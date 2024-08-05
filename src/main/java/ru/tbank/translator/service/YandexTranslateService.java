package ru.tbank.translator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class YandexTranslateService {
    @Value("${yandex.translate.api.key}")
    private String apiKey;

    private static final String YANDEX_TRANSLATE_API_URL =
            "https://translate.api.cloud.yandex.net/translate/v2/translate";

    private final RestTemplate restTemplate;

    public YandexTranslateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String translateRequest(String text, String sourceLang, String targetLang) {
        String requestBody = "{"
                + "\"sourceLanguageCode\":\"" + sourceLang + "\","
                + "\"targetLanguageCode\":\"" + targetLang + "\","
                + "\"texts\":[\"" + text + "\"]}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + apiKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);


        YandexTranslateResponse response = restTemplate.postForObject(YANDEX_TRANSLATE_API_URL, requestEntity,
                YandexTranslateResponse.class);
        if (response != null && response.getTranslations() != null && !response.getTranslations().isEmpty()) {
            return response.getTranslations().get(0).getText();
        } else {
            return "";
        }

    }

    public static class YandexTranslateResponse {
        private java.util.List<Translation> translations;

        public java.util.List<Translation> getTranslations() {
            return translations;
        }

        public void setTranslations(java.util.List<Translation> translations) {
            this.translations = translations;
        }
    }

    public static class Translation {
        private String text;
        private String detectedLanguageCode;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getDetectedLanguageCode() {
            return detectedLanguageCode;
        }

        public void setDetectedLanguageCode(String detectedLanguageCode) {
            this.detectedLanguageCode = detectedLanguageCode;
        }
    }
}
