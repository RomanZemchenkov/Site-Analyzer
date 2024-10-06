package searchengine.dao.repository.index;

class IndexSql {
    static final  String SAVE_INDEX_SQL = """
            INSERT INTO index(page_id, lemma_id, rank) 
            VALUES (:id,:lemma,:rank);
            """;

    static final String SELECT_INDEX_WITH_PAGE_BY_LEMMA = """
            SELECT i.id, i.rank, p.id, p.path, p.code, p.content
            FROM index AS i
            JOIN page AS p ON i.page_id = p.id
            WHERE i.lemma_id = :lemmaId
            """;
}
