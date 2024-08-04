package ru.tbank.translator.service;


import org.springframework.stereotype.Service;
import ru.tbank.translator.model.TranslationRequest;
import ru.tbank.translator.repository.TranslationRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class TranslationService {
    private final TranslationRepository translationRepository;
    private final YandexTranslateService yandexTranslateService;
    private final ExecutorService executorService;

    public TranslationService(TranslationRepository translationRepository,
                              YandexTranslateService yandexTranslateService) {
        this.translationRepository = translationRepository;
        this.yandexTranslateService = yandexTranslateService;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public String translateWords(TranslationRequest request) {
        List<String> wordsWithPunctuation = request.getElements();

        List<CompletableFuture<String>> translationFutures = wordsWithPunctuation.stream()
                .map(word -> CompletableFuture.supplyAsync(() -> {
                    if (word.matches("\\w+")) {
                        return yandexTranslateService.translateRequest(word, request.getSourceLang(),
                                request.getTargetLang());
                    } else {
                        return word;
                    }
                }, executorService))
                .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(translationFutures.toArray(new CompletableFuture[0]));

        String translatedText = allOf.thenApply(v -> translationFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.joining("")))
                .join();

        TranslationRequest savedRequest = new TranslationRequest();
        savedRequest.setIpAddress(request.getIpAddress());
        savedRequest.setText(request.getText());
        savedRequest.setTranslatedText(translatedText);
        translationRepository.saveTranslation(savedRequest);

        return translatedText;
    }
}
