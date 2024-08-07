# Word-translator

## Описание

Приложение для перевода набора слов с использованием внешнего сервиса Yandex Translator. Каждое слово переводится в
отдельном потоке. Максимальное количество одновременно работающих потоков ограничено 10.

## Требования

- Java 17 и выше
- Maven 3.6 и выше
- Реляционная база данных (MySQL, PostgreSQL, H2)

## Конфигурация


1. Создайте базу данных и укажите параметры подключения в файле `src/main/resources/application.properties`:

    ```properties
    spring.datasource.url=jdbc:your_database://host:port/database_name
    spring.datasource.username=your_database_username
    spring.datasource.password=your_database_password
    yandex.translate.api.key=your_yandex_translate_api_key
    spring.sql.init.mode=always
    spring.sql.init.schema-locations=classpath:schema.sql
    ```
   #### Пример заполненого файла:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/translation_db
   spring.datasource.username=postgres
   spring.datasource.password=123456789
   yandex.translate.api.key=AQ1Nzs8w4F3f03H6t2fT3uk4Jun3
   spring.sql.init.mode=always
   spring.sql.init.schema-locations=classpath:schema.sql
   ```

2. Настройте схему базы данных. Отредактируйте файл `src/main/resources/schema.sql` в соответствии с используемой базой данных, не изменяя название таблицы `translation_requests`. Примеры схемы для различных баз данных:
   #### Пример для MySQL и H2:
   ```sql
   CREATE TABLE IF NOT EXISTS translation_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(255),
    original_text TEXT,
    translated_text TEXT
   );
   ```
   #### Пример для PostgreSQL:
   ```sql
   CREATE TABLE IF NOT EXISTS translation_requests (
    id SERIAL PRIMARY KEY,
    ip_address VARCHAR(255),
    original_text TEXT,
    translated_text TEXT
   );
   ```
3. Соберите проект с помощью Maven:

    ```bash
    mvn clean install
    ```

4. Запустите приложение:

    ```bash
    mvn spring-boot:run
    ```

## Инструкция по запуску приложения через IntelliJ IDEA
1. Откройте проект в IntelliJ IDEA:
    - Запустите IntelliJ IDEA.

    - Выберите Open и укажите путь к директории проекта.


2. Настройка конфигурации запуска:

    - Перейдите в раздел Run -> Edit Configurations....

    - Нажмите на + и выберите Application или Spring Boot.

    - Укажите имя конфигурации (например, WordTranslatorApplication).

    - В поле Main class выберите ru.tbank.translator.TranslatorAppApplication.

    - Убедитесь, что в настройках проекта в IntelliJ IDEA указан правильный SDK (File -> Project Structure -> Project Settings -> Project -> SDK). Выберите JDK 17 или выше.

    - Нажмите Apply и OK.


3. Запустите приложение:
    - Выберите созданную конфигурацию и нажмите на зеленую кнопку Run или используйте сочетание клавиш Shift + F10.

Теперь приложение должно запуститься и будет доступно по адресу http://localhost:8080, если он не был намеренно изменён в файле `application.properties`. IP адрес, исходный и переведенный текст будут сохраняться в созданную таблицу `translation_requests`.

## Устрание проблем
- Убедитесь, что API ключ Yandex Translator и параметры подключения в `application.properties` верны и база данных запущена.
- Если приложение не запускается, так как порт 8080 уже используется другим приложением или закрыт, измените порт в файле application.properties:
    ```properties
    server.port=8081
    ```
  В данном случае адрес изменится на http://localhost:8081.

# API Перевода

## Инструкция по использованию

### Отправка POST-запроса

Для выполнения перевода отправьте POST-запрос на эндпоинт /translate с JSON-объектом в теле запроса:
```json
{
  "sourceLang": "en",
  "targetLang": "ru",
  "text": "Hello world, this is my first program"
}
```

### Примеры ответа:

**Успех (HTTP 200 OK):**

```text
Здравствуйте мир, этот является мой первый программа
```

**Ошибка языка (HTTP 400 Bad Request):**

```text
Не найден язык исходного сообщения
```

**Ошибка перевода (HTTP 400 Bad Request):**

```text
Ошибка доступа к ресурсу перевода
```

### Пример использования с cURL

Вы можете использовать cURL для отправки запроса:

```bash
curl -X POST "http://localhost:8080/translate" -H "Content-Type: application/json" -d '{"sourceLang": "en","targetLang": "ru","text": "Hello world, this is my first program"}'
```

Для командной строки Windows может потребоваться экранировать двойные кавычки:

```bash
curl -X POST "http://localhost:8080/translate" -H "Content-Type: application/json" -d "{\"sourceLang\": \"en\",\"targetLang\": \"ru\",\"text\": \"Hello world, this is my first program\"}"
```

### Пример использования с Postman

1. Откройте Postman.
2. Создайте новый POST-запрос к эндпоинту http://localhost:8080/translate.
3. В разделе "Body" выберите "raw" и "JSON".
4. Вставьте следующий JSON в поле ввода:

```json
{
  "sourceLang": "en",
  "targetLang": "ru",
  "text": "Hello world, this is my first program"
}
```

