package searchengine.dao.repository.lemma;

class LemmaSql {

    static final String WHERE = " WHERE ";
    static final String AND = " AND ";
    static final String BY_SITE_ID = " l.site_id = :id ";
    static final String BY_LEMMA = " l.lemma = :lemma ";
    static final String BY_LEMMAS = " l.lemma IN(:lemmas) ";

    static final String LEMMA_SAVE_SQL = """
            INSERT INTO lemma(site_id, lemma, frequency)
            VALUES (?,?,?);
            """;
    static final String SELECT_LEMMA =
            """
                    SELECT l.id,l.lemma,l.frequency
                    FROM lemma l""";

    static final String SELECT_LEMMA_BY_SITE_ID_AND_LEMMA = SELECT_LEMMA + WHERE + BY_SITE_ID + AND + BY_LEMMA;

    static final String SELECT_LEMMA_BY_SITE_ID_AND_LEMMA_SQL = SELECT_LEMMA + WHERE + BY_SITE_ID + AND + BY_LEMMAS;

    static final String UPDATE_FREQUENCY_FOR_EXIST_LEMMAS_SQL = """
            UPDATE lemma
            SET frequency = :frequency
            WHERE id = :id
            """;
}
