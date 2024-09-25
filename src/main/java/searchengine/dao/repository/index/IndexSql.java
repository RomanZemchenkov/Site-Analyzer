package searchengine.dao.repository.index;

class IndexSql {
    static final  String SAVE_INDEX_SQL = """
            INSERT INTO index(page_id, lemma_id, rank) 
            VALUES (:id,:lemma,:rank);
            """;

    static final  String ALL_PAGES_ID_SELECT_SQL = """
            SELECT i.page_id
            FROM index AS i
            WHERE i.id IN(:ids)
            """;
}
