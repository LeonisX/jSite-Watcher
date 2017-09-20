# jSite-Watcher

Аналог коммерческой программы Website-Watcher с открытым исходным кодом.

Пишется "под себя", поскольку, WsW является достаточно навороченныи продуктом,
и, к тому же, платным.

# TODO список

Сейчас стоит задача максимально быстро запустить jSite-Watcher в работу. 
Для этого необходимо реализовать минимальный функционал.

###Фаза 0

- [x] Функция пределение того, что страница изменилась (пока просто equals)

###Фаза 1

- [x] Таблица закладок (H2): ID, categoryId, URL, title, date, settings
- [x] Сохранять страницу в файл {ID}
- [x] Добавлять закладки
- [x] Таблица категорий (H2): ID, parentId, title, settings
- [x] Пока одна категория All

###Фаза 2

- [x] Проверка всех по требованию (пока просто кнопка)
- [ ] Подсветка изменений
- [ ] Отображение с подсветкой изменённых данных во встроенном браузере

##О Website-Watcher

WebSite-Watcher — условно-бесплатная программа с закрытым кодом, 
отслеживающая изменения на заданных пользователем веб-страницах. 
Работает под ОС Windows, WINE.

WebSite-Watcher, фактически, является менеджером закладок с функциями наблюдения за изменениями. 
Благодаря поддержке языка регулярных выражений, в закладке можно указать область отслеживаемых изменений.

Несмотря на ряд недостатков, программа популярна среди медиа аналитиков. 
После настройки она позволяет практически полностью автоматизировать мониторинг СМИ.

##Функции Website-Watcher

- Наблюдение за изменениями заданных пользователем веб-страниц.
- Подсветка изменений на странице
- Поддержка регулярных выражений
- Импорт ссылок из текстового файла
- Экспорт закладок во внешний файл
- Фильтрация javascript
- Чтение ленты RSS- или Atom-новостей
- Локализация интерфейса на многие языки

##Возможности Website-Watcher

- Мониторинг веб-страниц всех типов. При этом в зависимости от настройки программа подсвечивает только измененные фрагменты страницы, либо только заданные ключевые слова, либо исключительно те изменившиеся части страницы, где есть заданные ключевые слова;
- Мониторинг защищенных паролем страниц. При помощи специально встроенного инструмента вы однажды записываете для защищенных паролем страниц логины, и дальше всю работу программа выполняет самостоятельно.
- Мониторинг форумов. Позволяет следить за появлением новых тем и ответов на уже имеющиеся, интересующие вас темы. Поддерживает подавляющую часть популярных форумных движков;
- Мониторинг RSS лент. Программа обнаруживает новые или измененные RSS сообщения и преобразует их в обычный текстовой формат;
- Мониторинг групп новостей. Группы новостей слабо развиты в Рунете, но очень популярны в мировом интернете. Программа позволяет вести мониторинг групп новостей по заданным ключевым словам;
- Мониторинг локальных файлов. Позволяет использовать в локальной сети или на собственном компьютере те же мощные функции, что и для работы с веб-ресурсами;
- Мониторинг документов. Программа автоматически преобразует PDF, Word, Excel документы в HTML файлы и дальше работает с ними как с обычными веб-страницами, ведет поиск по ключевым словам и т.п.
