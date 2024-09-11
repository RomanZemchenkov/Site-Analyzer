package searchengine.dao.repository.index;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import searchengine.dao.model.Index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CustomIndexRepositoryImpl implements CustomIndexRepository{

    private final EntityManager entityManager;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final static String SAVE_INDEX_SQL =
            """
            INSERT INTO index(page_id, lemma_id, rank) 
            VALUES (:id,:lemma,:rank);
            """;

    @Override
    public void createBatch(List<Index> indexList) {
        int batchSize = 1000;
        List<Index> tempList = new ArrayList<>();
        for(int i = 0; i < indexList.size(); i++){
            tempList.add(indexList.get(i));
            if(i % batchSize == 0){
                create(tempList);
                tempList = new ArrayList<>();
            }
        }
        create(tempList);
        System.out.println("Сохранение индексов произошло");
    }

    public void create(List<Index> indexList){
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
