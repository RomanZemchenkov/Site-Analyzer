package searchengine.dao.repository.index;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import searchengine.dao.model.Index;
import searchengine.dao.model.Page;

import java.util.ArrayList;
import java.util.List;

import static searchengine.dao.repository.index.IndexSql.ALL_PAGES_ID_SELECT_SQL;
import static searchengine.dao.repository.index.IndexSql.SAVE_INDEX_SQL;

@RequiredArgsConstructor
public class CustomIndexRepositoryImpl implements CustomIndexRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private static final int BATCH_SIZE = 1000;

    @Override
    public void batchSave(List<Index> indexList) {
        List<Index> tempList = new ArrayList<>();
        for (Index index : indexList) {
            tempList.add(index);

            if (tempList.size() == BATCH_SIZE) {
                save(tempList);
                tempList.clear();
            }
        }
        if (!tempList.isEmpty()) {
            save(tempList);
        }
    }

    @Override
    public List<Integer> getAllPagesId(List<Index> indexList) {
        List<Integer> ids = indexList.stream().map(Index::getId).toList();

        MapSqlParameterSource source = new MapSqlParameterSource("ids", ids);

        return jdbcTemplate.query(ALL_PAGES_ID_SELECT_SQL,source,(rs, row) -> rs.getInt("page_id"));
    }

    @Override
    public void deleteAllByPage(Page page) {
        Integer id = page.getId();
        int batchSize = 1000;
        int batchCounter = 0;
        int counter = 1;
        while (counter != 0) {
            int i = entityManager.createQuery("DELETE FROM Index i WHERE i.page.id = :pageId")
                    .setParameter("pageId", id)
                    .executeUpdate();
            batchCounter++;
            counter = i;
            if (batchCounter == batchSize) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    private void save(List<Index> indexList) {
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
