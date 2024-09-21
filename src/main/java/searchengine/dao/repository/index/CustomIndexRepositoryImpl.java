package searchengine.dao.repository.index;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CustomIndexRepositoryImpl implements CustomIndexRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final  String SAVE_INDEX_SQL = """
            INSERT INTO index(page_id, lemma_id, rank) 
            VALUES (:id,:lemma,:rank);
            """;

    private static final  String ALL_PAGES_ID_SELECT_SQL = """
            SELECT i.page_id
            FROM index AS i
            WHERE i.id IN(:ids)
            """;

    @Override
    public void createBatch(List<Index> indexList) {
        int batchSize = 1000;
        List<Index> tempList = new ArrayList<>();
        for (Index index : indexList) {
            tempList.add(index);

            if (tempList.size() == batchSize) {
                create(tempList);
                tempList.clear();
            }
        }
        if (!tempList.isEmpty()) {
            create(tempList);
        }
        System.out.println("Сохранение индексов произошло");
    }

    @Override
    public List<Integer> getAllPagesId(List<Index> indexList) {
        List<Integer> ids = indexList.stream().map(Index::getId).toList();

        MapSqlParameterSource source = new MapSqlParameterSource("ids", ids);

        return jdbcTemplate.query(ALL_PAGES_ID_SELECT_SQL,source,(rs, row) -> rs.getInt("page_id"));
    }

    private void create(List<Index> indexList) {
        SqlParameterSource[] batchParams = new SqlParameterSource[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            Index index = indexList.get(i);
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", index.getPage().getId());
            params.addValue("lemma", index.getLemma().getId());
            params.addValue("rank", index.getRank());
            batchParams[i] = params;
        }

        jdbcTemplate.batchUpdate(SAVE_INDEX_SQL, batchParams);
    }
}
