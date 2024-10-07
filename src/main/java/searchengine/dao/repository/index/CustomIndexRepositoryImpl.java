package searchengine.dao.repository.index;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import searchengine.aop.annotation.CheckTimeWorking;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static searchengine.dao.repository.index.IndexSql.SAVE_INDEX_SQL;
import static searchengine.dao.repository.index.IndexSql.SELECT_INDEX_WITH_PAGE_BY_LEMMA;

@RequiredArgsConstructor
public class CustomIndexRepositoryImpl implements CustomIndexRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private static final int BATCH_SIZE = 1000;

    @Override
    @CheckTimeWorking
    public List<Index> findAllIndexesWithPageByLemmas(Lemma lemma) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("lemmaId", lemma.getId());
        Site site = lemma.getSite();
        return jdbcTemplate.query(SELECT_INDEX_WITH_PAGE_BY_LEMMA, source, (rs, rowNum) -> {
            Page page = createPageFactory(rs, site);

            Index index = new Index();
            index.setId(rs.getInt(1));
            index.setPage(page);
            index.setRank(rs.getFloat(2));
            index.setLemma(lemma);

            page.getIndexes().add(index);
            lemma.getIndexes().add(index);
            return index;
        });
    }

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
        System.out.println("Сохранение индексов произошло");
    }

    @Override
    @CheckTimeWorking
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

    private Page createPageFactory(ResultSet rs, Site site) {
        Page page = new Page();
        try {
            page.setId(rs.getInt(3));
            page.setPath(rs.getString(4));
            page.setCode(rs.getInt(5));
            page.setContent(rs.getString(6));
            page.setSite(site);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return page;
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
