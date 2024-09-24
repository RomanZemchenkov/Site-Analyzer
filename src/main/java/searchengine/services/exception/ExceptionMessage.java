package searchengine.services.exception;

public class ExceptionMessage {

    public static final String ILLEGAL_PAGE_EXCEPTION = "Данная страница находится за пределами сайтов, указанных в конфигурационном файле.";
    public static final String INDEXING_STARTING_EXCEPTION = "Индексация не окончена. Пожалуйста, подождите.";
    public static final String EMPTY_QUERY_EXCEPTION = "Задан пустой поисковый запрос.";
    public static final String SITE_DOESNT_EXIST_EXCEPTION = "Данный сайт не проиндексирован, либо отсутствует в списке индексируемых сайтов.";
}
