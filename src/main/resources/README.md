# Репозиторий
- код должен разрабатывать в отдельной ветке 
- код должен быть выложен в виде pull request
- код должен быть покрыт тестами(целевое покрытие 75%)
- код должен быть "чистым" (как минимум: sonarLint)
- в рамках одной ветки должна решаться одна проблема.
- рекомендуемый размер PR(pull request) < 20 файлов 
- "сливать" PR без ревью **ЗАПРЕЩЕНО** (увы, бесплатная версия накладывает ряд ограничений. 
публичным репозиторий делать не хочется)
- PR должен содержать ссылку на задачу. Пример: 000 https://github.com/sovaowl95/sovaowlRU/projects/2#card-37446562

# Задачи
 - брать задачи можно на доске https://github.com/sovaowl95/sovaowlRU/projects/2
 - после того, как взяли задачу, необходимо её перенести в колонку **In progress**
 - после того, как взяли задачу, необходимо сообщить об этом в telegram канал
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