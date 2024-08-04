# Translator

## Описание

Приложение для перевода набора слов с использованием внешнего сервиса перевода Yandex. Каждое слово переводится в отдельном потоке, количество одновременно работающих потоков не более 10.

## Требования

- Java 17
- Maven 3.6+
- PostgreSQL

## Конфигурация

1. Создайте базу данных PostgreSQL и укажите параметры подключения в файле `application.properties`:

    ```properties
    spring.datasource.url=jdbc:postgresql://your_database_url
    spring.datasource.username=your_database_username
    spring.datasource.password=your_database_password
    yandex.translate.api.key=your_yandex_translate_api_key
    ```

2. Соберите проект с помощью Maven:

    ```bash
    mvn clean install
    ```

3. Запустите приложение:

    ```bash
    mvn spring-boot:run
    ```

# API Перевода

## Инструкция по использованию

### Отправка POST-запроса

Отправьте текстовый POST-запрос на эндпоинт `/translate` со следующим телом запроса:

```text
en → ru
Hello world, this is my first program
```
Исходный и целевой языки должны быть разделены символом "→".

Текст для перевода должен быть расположен на следующей строке после языковой пары.

### Пример ответа
```text
Здравствуйте мир, этот является мой первый программа
```
### Пример использования с cURL
Вы можете использовать cURL для отправки запроса:
```text
curl -X POST "http://localhost:8080/translate" -d $'en → ru\nHello world, this is my first program'
```
### Пример использования с Postman
1. Откройте Postman.
2. Создайте новый POST-запрос к эндпоинту http://localhost:8080/translate.
3. В разделе "Body" выберите "raw" и "Text".
4. Вставьте следующий текст в поле ввода:
```text
en → ru
Hello world, this is my first program
```

