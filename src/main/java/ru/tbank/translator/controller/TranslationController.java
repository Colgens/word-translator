package ru.tbank.translator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import ru.tbank.translator.model.TranslationRequest;
import ru.tbank.translator.service.TranslationService;
import ru.tbank.translator.service.YandexLanguageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/translate")
public class TranslationController {

    private final TranslationService translationService;
    private final YandexLanguageService yandexLanguageService;

    public TranslationController(TranslationService translationService, YandexLanguageService yandexLanguageService) {
        this.translationService = translationService;
        this.yandexLanguageService = yandexLanguageService;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> translate(@RequestBody String requestBody) {

        TranslationRequest request = parseJson(requestBody);

        if (request == null) {
            return ResponseEntity.badRequest().body("Некорректный формат JSON");
        }

        String sourceLanguage = request.getSourceLang();
        String targetLanguage = request.getTargetLang();
        String text = request.getText();

        Set<String> availableLanguages = yandexLanguageService.getAvailableLanguages();

        if (sourceLanguage == null || sourceLanguage.isEmpty()) {
            return ResponseEntity.badRequest().body("Исходный язык не указан");
        }
        if (targetLanguage == null || targetLanguage.isEmpty()) {
            return ResponseEntity.badRequest().body("Целевой язык не указан");
        }
        if (!availableLanguages.contains(sourceLanguage)) {
            return ResponseEntity.badRequest().body("Не найден язык исходного сообщения");
        }

        if (!availableLanguages.contains(targetLanguage)) {
            return ResponseEntity.badRequest().body("Не найден целевой язык для перевода");
        }

        if (text == null || text.isEmpty()) {
            return ResponseEntity.badRequest().body("Текст не может быть пустым");
        }

        request.setElements(splitTextIntoWordsAndPunctuationMarks(text));
        request.setIpAddress(getClientIp());
        return ResponseEntity.ok(translationService.translateWords(request));
    }

    private TranslationRequest parseJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, TranslationRequest.class);
        } catch (Exception e) {
            return null;
        }
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

    public List<String> splitTextIntoWordsAndPunctuationMarks(String text) {
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
