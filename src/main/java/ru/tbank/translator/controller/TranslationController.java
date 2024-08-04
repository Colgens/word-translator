package ru.tbank.translator.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.tbank.translator.model.TranslationRequest;
import ru.tbank.translator.service.TranslationService;
import ru.tbank.translator.service.YandexLanguageService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class TranslationController {

    private final TranslationService translationService;
    private final YandexLanguageService yandexLanguageService;

    public TranslationController(TranslationService translationService, YandexLanguageService yandexLanguageService) {
        this.translationService = translationService;
        this.yandexLanguageService = yandexLanguageService;
    }

    @PostMapping(value = "/translate", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> translate(@RequestBody String requestBody) {
        String[] parts = requestBody.split("\\n");
        if (parts.length != 2) {
            return ResponseEntity.badRequest().body("Некорректный формат запроса");
        }
        if (!parts[0].contains("→")) {
            return ResponseEntity.badRequest().body("Исходный и целевой языки должны быть разделены символом '→'");
        }
        String[] languages = parts[0].split("→");
        if (languages.length != 2) {
            return ResponseEntity.badRequest().body("Некорректный формат запроса");
        }
        String sourceLanguage = languages[0].trim();
        String targetLanguage = languages[1].trim();
        if (!yandexLanguageService.getAvailableLanguages().contains(sourceLanguage)) {
            return ResponseEntity.badRequest().body("Не найден язык исходного сообщения");
        }
        if (!yandexLanguageService.getAvailableLanguages().contains(targetLanguage)) {
            return ResponseEntity.badRequest().body("Не найден целевой язык для перевода");
        }



        TranslationRequest request = new TranslationRequest();
        request.setSourceLang(languages[0].trim());
        request.setTargetLang(languages[1].trim());
        request.setText(parts[1].trim());
        request.setElements(splitTextIntoWordsWithPunctuation(request.getText()));
        request.setIpAddress(getClientIp());
        return ResponseEntity.ok(translationService.translateWords(request));
    }


    private String getClientIp() {
        String[] ipHeaders = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        ServletRequestAttributes requestAttributes;
        requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        String ipAddress;
        for (String header : ipHeaders) {
            ipAddress = request.getHeader(header);
            if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
                return ipAddress;
            }
        }
        return request.getRemoteAddr();
    }


    public List<String> splitTextIntoWordsWithPunctuation(String text) {
        List<String> parts = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\w+|\\p{Punct}\\s*");
        Matcher matcher = pattern.matcher(text);
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                parts.add(text.substring(lastEnd, matcher.start()));
            }
            parts.add(matcher.group());
            lastEnd = matcher.end();
        }
        if (lastEnd < text.length()) {
            parts.add(text.substring(lastEnd));
        }
        return parts;
    }


}
