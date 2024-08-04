package ru.tbank.translator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class YandexLanguageService {
    @Value("${yandex.translate.api.key}")
    private String apiKey;

    private static final String YANDEX_LANGUAGES_API_URL =
            "https://translate.api.cloud.yandex.net/translate/v2/languages";

    private Set<String> availableLanguages;

    public Set<String> getAvailableLanguages() {
        if (availableLanguages == null) {
            refreshAvailableLanguages();
        }
        return availableLanguages;
    }

    public void refreshAvailableLanguages() {
        RestTemplate restTemplate = new RestTemplate();

        String requestBody = "{}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + apiKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        YandexLanguagesResponse response = restTemplate.postForObject(YANDEX_LANGUAGES_API_URL, requestEntity,
                YandexLanguagesResponse.class);
        if (response != null) {
            availableLanguages = response.getLanguages()
                    .stream()
                    .map(Language::getCode)
                    .collect(Collectors.toCollection(HashSet<String>::new));
        }
    }

    public static class YandexLanguagesResponse {
        private java.util.List<Language> languages;

        public java.util.List<Language> getLanguages() {
            return languages;
        }

        public void setLanguages(java.util.List<Language> languages) {
            this.languages = languages;
        }
    }

    public static class Language {
        private String code;
        private String name;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
