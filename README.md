# Репозиторий
- код должен разрабатывать в **ОТДЕЛЬНОЙ ВЕТКЕ** 
- код должен быть выложен в виде PR(pull request)
- код должен быть покрыт тестами(целевое покрытие 75%)
- код должен быть "чистым" (как минимум: sonarLint)
- в рамках одной ветки должна решаться одна проблема.
- рекомендуемый размер PR < 20 файлов 
- "сливать" PR без ревью **ЗАПРЕЩЕНО** (увы, бесплатная версия накладывает ряд ограничений. 
публичным репозиторий делать не хочется)
- PR должен содержать ссылку на задачу. Пример: 000 https://github.com/sovaowl95/sovaowlRU/projects/2#card-37446562

# Задачи
 - брать задачи можно на досках https://github.com/sovaowl95/sovaowlRU/projects/2 https://github.com/sovaowl95/sovaowlRU/projects/3
 - после того, как взяли задачу, необходимо её перенести в колонку **In progress**
 - после того, как взяли задачу, необходимо сообщить об этом в telegram канал https://t.me/joinchat/NJYQJRjA4ayN0gvQejdbPg
 - после того, как взяли задачу, необходимо добавить расчетное время выполнения задачи
 - если в процессе разработки обнаружились новые проблемы, необходимо добавить задачу на доску в столбец **To do**

# Запуск:

для запуска необходимо задать ряд параметров: 
```
SS
-Xms500m

 --db_url=
 --db_username=
 --db_password=
 --db_schema=
 --cert_alias=
 --cert_path=
 --cert_password=
 --discord_ClientId=
 --discord_ClientSecret=
 --discord_BotToken=
 --spring.mail.username=
 --spring.mail.password=
 --gg_clientSecret=
 --gg_clientId=
 --google_clientId=
 --google_clientSecret=
 --qiwi_clientId=
 --qiwi_clientSecret=
 --twitch_clientId=
 --twitch_clientSecret=
 --vk_clientId=
 --vk_clientSecret=
```

## обязательные: 
 - db_* - postgres 11.5
 - SS (указывает, что запуск производится вручную. нужно для деплоя на амаозон)
 - -Xms500m (ограничивает память приложения 500мб)

## необязательные:
 - cert_* - https://www.baeldung.com/spring-boot-https-self-signed-certificate
 - discord_* - https://discordapp.com/developers/applications
 - spring.mail.* - https://admin.yandex.ru/mail/register/organizations
 - gg_ - https://github.com/GoodGame/API/blob/master/Streams/v2/authentication.md
 - google_* - https://console.developers.google.com/apis/dashboard
 - qiwi_* - https://qiwi.com/p2p-admin/transfers/api
 - twitch_* - https://dev.twitch.tv/console подать заявку
 - vk_* - https://vk.com/apps?act=manage (standalone приложение)
##### значения этих параметров можно задать равными единице!
##### java -jar sovaowl.jar --vk_clientSecret=1 --vk_clientId=1 ...

## софт:
 - jdk 11 (любая версия. на проде https://aws.amazon.com/ru/corretto/ jdk11)
 - apache maven (3.6.3) https://maven.apache.org/install.html#
 - среда разработки (я пользуюсь intellij idea с рядом плагинов)

### запуск без SSL
```
1. в package ru.sovaowltv.config; закомментировать класс SslConfiguration (полностью).
сайт будет доступен по ссылке: http://localhost/
```

### пример запуска:
 - собрать проект
```bash
mvn clean
mvn package
```

 - jar будет лежать в папке target(обязательны 4 параметра)
```bash

cd target

java -jar sovaowltv-1.0-SNAPSHOT.jar SS --spring.profiles.active=local --db_url=jdbc:postgresql://localhost:5432/ИМЯ_БД --db_username=ТУТ_ИМЯ --db_password=ТУТ_ПАРОЛЬ --db_schema=ТУТ_СХЕМА --discord_ClientId=1 --discord_ClientSecret=1 --discord_BotToken=1 --spring.mail.username=1 --spring.mail.password=1 --gg_clientSecret=1 --gg_clientId=1 --google_clientId=1 --google_clientSecret=1 --qiwi_clientId=1 --qiwi_clientSecret=1 --twitch_clientId=1 --twitch_clientSecret=1 --vk_clientId=1 --vk_clientSecret=1 --vk_clientSecret=1
```
 - если проект запускается через idea, то необходимо строку выше добавить в **PROGRAM ARGUMENTS**
#
для некоторых параметров понадобиться завести ряд аккаунтов. 
можно игнорировать их и(или) закомментировать ненужный для разработки код

возможно, потребуется создать в бд таблицы для хранения сессий
```sql
CREATE TABLE SPRING_SESSION (
  PRIMARY_ID CHAR(36) NOT NULL,
  SESSION_ID CHAR(36) NOT NULL,
  CREATION_TIME BIGINT NOT NULL,
  LAST_ACCESS_TIME BIGINT NOT NULL,
  MAX_INACTIVE_INTERVAL INT NOT NULL,
  EXPIRY_TIME BIGINT NOT NULL,
  PRINCIPAL_NAME VARCHAR(100),
  CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);
CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
  SESSION_PRIMARY_ID CHAR(36) NOT NULL,
  ATTRIBUTE_NAME VARCHAR(180) NOT NULL,
  ATTRIBUTE_BYTES BLOB NOT NULL,
  CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
  CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);
```
